package com.iprody.crm.paymentservice.kafka.listener;

import com.iprody.crm.paymentservice.kafka.model.PaymentRequestEvent;
import com.iprody.crm.paymentservice.service.OutboxCreator;
import com.iprody.crm.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestEventListener {

    private final OutboxCreator outboxCreator;
    private final PaymentService paymentService;

    @KafkaListener(
            topics = {"${kafka.topic.payments.requests}"},
            groupId = "${kafka.consumer.payment.request.group}",
            containerFactory = "paymentRequestListenerContainerFactory"
    )
    public void consumePaymentEvent(ConsumerRecord<String, PaymentRequestEvent> record) {
        String orderId = record.key();
        PaymentRequestEvent event = record.value();

        log.info("Received payment request for order: {}", orderId);

        if (paymentService.existsByInquiryRefId(UUID.fromString(orderId))) {
            log.info("Order {} already exists, skipping duplicate request", orderId);
            return;
        }

        outboxCreator.createIncomingPaymentMessage(orderId, event);
        log.info("Saved payment request for order {} to outbox", orderId);
    }
}
