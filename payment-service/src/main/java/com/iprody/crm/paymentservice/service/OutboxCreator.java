package com.iprody.crm.paymentservice.service;

import com.iprody.crm.paymentservice.kafka.model.PaymentRequestEvent;
import com.iprody.crm.paymentservice.model.entity.OutboxMessage;
import com.iprody.crm.paymentservice.model.entity.Payment;
import com.iprody.crm.paymentservice.model.enums.PaymentStateEvent;
import com.iprody.crm.paymentservice.model.enums.PaymentStatus;
import com.iprody.crm.paymentservice.repository.OutboxRepository;
import com.iprody.crm.paymentservice.repository.PaymentRepository;
import com.iprody.crm.paymentservice.statemachine.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxCreator {

    private final OutboxRepository outboxRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final PaymentStateMachine stateMachine;

    @Transactional
    public void createIncomingPaymentMessage(String orderId, PaymentRequestEvent event) {
        Payment payment = createPaymentRecord(event);

        OutboxMessage message = OutboxMessage.builder()
                .event(objectMapper.writeValueAsString(event))
                .aggregateId(UUID.fromString(orderId))
                .type(OutboxMessage.OutboxType.INCOMING_PAYMENT)
                .status(OutboxMessage.OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        outboxRepository.save(message);
        log.debug("Created incoming payment and outbox message for order: {}", orderId);
    }

    @Transactional
    public void updateMessageForStatusCheck(OutboxMessage message, Payment payment) {
        message.setStatus(OutboxMessage.OutboxStatus.PENDING);
        message.setType(OutboxMessage.OutboxType.STATUS_CHECK);
        message.setAggregateId(payment.getInquiryRefId());
        message.setPaymentId(payment.getId());
        message.setTransactionId(payment.getTransactionRefId());
        message.setRetryCount(0);
        message.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(message);
        log.debug("Created status check message for orderId:{} with paymentId: {}", message.getAggregateId(),  message.getPaymentId());
    }

    private Payment createPaymentRecord(PaymentRequestEvent event) {
        Payment payment = new Payment();
        payment.setInquiryRefId(event.getInquiryRefId());
        payment.setAmount(event.getAmount());
        payment.setCurrency(event.getCurrency());
        payment.setPaymentStatus(PaymentStatus.RECEIVED);
        payment.setState(stateMachine.transition(null, PaymentStateEvent.CREATE));
        payment.setCreatedAt(Timestamp.from(Instant.now()));
        payment.setUpdatedAt(Timestamp.from(Instant.now()));
        return payment;
    }
}