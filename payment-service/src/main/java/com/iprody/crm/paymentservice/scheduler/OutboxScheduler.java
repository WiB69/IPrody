package com.iprody.crm.paymentservice.scheduler;

import com.iprody.crm.paymentservice.service.PaymentOutboxProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final PaymentOutboxProcessor paymentOutboxProcessor;

    @Value("${outbox.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(fixedDelayString = "${outbox.scheduler.fixed-delay:1000}")
    @Async("incomingPaymentTaskScheduler")
    public void processIncomingPayments() {
        if (!schedulerEnabled) {
            return;
        }
        paymentOutboxProcessor.processIncomingPayments();

    }

    @Scheduled(fixedDelayString = "${outbox.scheduler.status-check-delay:1000}")
    @Async("statusCheckTaskScheduler")
    public void processStatusChecks() {
        if (!schedulerEnabled) {
            return;
        }
        paymentOutboxProcessor.processStatusChecks();

    }
}