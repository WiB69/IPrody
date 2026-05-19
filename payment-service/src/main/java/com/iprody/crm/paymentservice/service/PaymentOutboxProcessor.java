package com.iprody.crm.paymentservice.service;

import com.iprody.crm.paymentservice.exception.PaymentException;
import com.iprody.crm.paymentservice.kafka.model.PaymentRequestEvent;
import com.iprody.crm.paymentservice.kafka.model.PaymentResponseEvent;
import com.iprody.crm.paymentservice.model.dto.XPaymentRequest;
import com.iprody.crm.paymentservice.model.dto.XPaymentResponse;
import com.iprody.crm.paymentservice.model.entity.OutboxMessage;
import com.iprody.crm.paymentservice.model.entity.Payment;
import com.iprody.crm.paymentservice.model.enums.PaymentState;
import com.iprody.crm.paymentservice.model.enums.PaymentStateEvent;
import com.iprody.crm.paymentservice.model.enums.PaymentStatus;
import com.iprody.crm.paymentservice.repository.OutboxRepository;
import com.iprody.crm.paymentservice.repository.PaymentRepository;
import com.iprody.crm.paymentservice.statemachine.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.iprody.crm.paymentservice.model.dto.XPaymentResponse.Status.CANCELED;
import static com.iprody.crm.paymentservice.model.dto.XPaymentResponse.Status.SUCCEEDED;
import static com.iprody.crm.paymentservice.model.enums.PaymentStatus.APPROVED;
import static com.iprody.crm.paymentservice.model.enums.PaymentStatus.DECLINED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProviderService paymentProviderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxCreator outboxCreator;
    private final PaymentStateMachine stateMachine;
    private final PaymentService paymentService;

    @Value("${outbox.batch-size:10}")
    private int batchSize;

    @Value("${outbox.max-retry-count:3}")
    private int maxRetryCount;

    @Value("${kafka.topic.payments.response}")
    private String paymentResponseTopic;

    @Value("${kafka.topic.payments.dlq}")
    private String dlqTopic;

    @Transactional
    public void processIncomingPayments() {
        List<OutboxMessage> pendingMessages = outboxRepository.findPendingMessages(
                OutboxMessage.OutboxType.INCOMING_PAYMENT,
                OutboxMessage.OutboxStatus.PENDING,
                maxRetryCount,
                PageRequest.of(0, batchSize)
        );

        for (OutboxMessage message : pendingMessages) {
            try {
                UUID orderId = message.getAggregateId();
                PaymentRequestEvent event = objectMapper.readValue(message.getEvent(), PaymentRequestEvent.class);

                Payment payment = paymentService.findByInquiryRefId(orderId);

                XPaymentRequest xRequest = buildXPaymentRequest(event, message.getAggregateId().toString());
                XPaymentResponse response = paymentProviderService.initiatePayment(xRequest);

                payment.setTransactionRefId(response.getId());
                payment.setState(stateMachine.transition(payment.getState(), PaymentStateEvent.PROCESS));
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment.setUpdatedAt(Timestamp.from(Instant.now()));
                paymentRepository.save(payment);

                outboxCreator.updateMessageForStatusCheck(message, payment);

            } catch (Exception e) {
                log.error("Failed to process incoming payment message: {}", message.getId(), e);
                handleProcessingError(message, e);
            }
        }
    }

    @Transactional
    public void processStatusChecks() {
        List<OutboxMessage> pendingMessages = outboxRepository.findPendingMessages(
                OutboxMessage.OutboxType.STATUS_CHECK,
                OutboxMessage.OutboxStatus.PENDING,
                maxRetryCount,
                PageRequest.of(0, batchSize)
        );

        for (OutboxMessage message : pendingMessages) {
            try {
                UUID transactionId = message.getTransactionId();
                UUID orderId = message.getAggregateId();

                XPaymentResponse response = paymentProviderService.checkPaymentStatus(transactionId);

                Payment payment = paymentService.findByInquiryRefId(orderId);

                if (!handlePaymentStatus(response, payment, message, orderId)) {
                    continue;
                }

                payment.setUpdatedAt(Timestamp.from(Instant.now()));
                paymentRepository.save(payment);
                outboxRepository.save(message);

                log.info("Status check completed for order: {}, status: {}", orderId, response.getStatus());

            } catch (Exception e) {
                log.error("Failed to process status check message: {}", message.getId(), e);
                handleProcessingError(message, e);
            }
        }
    }

    private void finalizePayment(Payment payment,
                                 OutboxMessage message,
                                 UUID orderId,
                                 XPaymentResponse response,
                                 PaymentState state,
                                 PaymentStatus paymentStatus) {
        payment.setState(state);
        payment.setPaymentStatus(paymentStatus);
        String eventStatus = state.equals(PaymentState.SUCCEEDED) ? "succeeded" : "failed";

        PaymentResponseEvent responseEvent = PaymentResponseEvent.builder()
                .order(orderId)
                .status(eventStatus)
                .transactionId(response.getId())
                .build();
        kafkaTemplate.send(paymentResponseTopic, orderId.toString(), responseEvent);
        message.setStatus(OutboxMessage.OutboxStatus.PROCESSED);
        message.setProcessedAt(LocalDateTime.now());
    }

    private XPaymentRequest buildXPaymentRequest(PaymentRequestEvent event, String orderId) {
        return XPaymentRequest.builder()
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .customer(event.getCustomer())
                .order(orderId)
                .receiptEmail(event.getReceiptEmail())
                .build();
    }

    private void handleProcessingError(OutboxMessage message, Exception e) {
        message.setRetryCount(message.getRetryCount() + 1);

        if (message.getRetryCount() >= maxRetryCount) {
            try {
                if (message.getPaymentId() != null) {
                    paymentRepository.findById(message.getPaymentId())
                            .ifPresent(payment -> transitionPaymentToDlqState(payment));
                }

                String key = message.getAggregateId() != null ? message.getAggregateId().toString() : null;
                kafkaTemplate.send(dlqTopic, key, message.getEvent());
                message.setStatus(OutboxMessage.OutboxStatus.FAILED);
                message.setProcessedAt(LocalDateTime.now());
                log.warn("Message {} exceeded max retries ({}), sent to DLQ",
                        message.getId(), maxRetryCount);

            } catch (Exception ex) {
                log.error("Failed to send message {} to DLQ", message.getId(), ex);
                message.setStatus(OutboxMessage.OutboxStatus.FAILED);
                message.setProcessedAt(LocalDateTime.now());
            }
        } else {
            log.debug("Message {} will be retried. Attempt: {}/{}",
                    message.getId(), message.getRetryCount(), maxRetryCount);
        }

        outboxRepository.save(message);
    }

    private void transitionPaymentToDlqState(Payment payment) {
        try {
            if (payment.getState() != PaymentState.SUCCEEDED &&
                    payment.getState() != PaymentState.FAILED) {
                payment.setState(stateMachine.transition(
                        payment.getState(),
                        PaymentStateEvent.SEND_TO_DLQ
                ));
                payment.setUpdatedAt(Timestamp.from(Instant.now()));
                paymentRepository.save(payment);
                log.info("Payment {} moved to DLQ state", payment.getId());
            }
        } catch (PaymentException stateEx) {
            log.error("Failed to transition payment {} to DLQ state", payment.getId(), stateEx);
        }
    }

    private boolean handlePaymentStatus(XPaymentResponse response,
                                        Payment payment,
                                        OutboxMessage message,
                                        UUID orderId) {
        if (SUCCEEDED.equals(response.getStatus())) {
            PaymentState newState = stateMachine.transition(payment.getState(), PaymentStateEvent.SUCCEED);
            finalizePayment(payment, message, orderId, response, newState, APPROVED);
            return true;
        } else if (CANCELED.equals(response.getStatus())) {
            PaymentState newState = stateMachine.transition(payment.getState(), PaymentStateEvent.FAIL);
            finalizePayment(payment, message, orderId, response, newState, DECLINED);
            return true;
        } else {
            log.info("Payment {} still processing, will retry later", payment.getId());
            return false;
        }
    }
}