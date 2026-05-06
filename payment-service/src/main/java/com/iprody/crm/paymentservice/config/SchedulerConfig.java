package com.iprody.crm.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean(name = "incomingPaymentTaskScheduler")
    public TaskScheduler incomingPaymentTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("incoming-payment-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean(name = "statusCheckTaskScheduler")
    public TaskScheduler statusCheckTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("status-check-");
        scheduler.initialize();
        return scheduler;
    }
}
