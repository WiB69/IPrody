package com.iprody.crm.paymentservice.kafka.config;

import com.iprody.crm.paymentservice.kafka.model.PaymentRequestEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.payment.request.group}")
    private String paymentRequestConsumerGroup;

    @Value("${kafka.consumer.auto.offset.reset:earliest}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.session.timeout.ms:45000}")
    private String sessionTimeoutMs;

    @Value("${kafka.consumer.max.poll.interval.ms:300000}")
    private String maxPollIntervalMs;

    @Value("${kafka.consumer.max.poll.records:500}")
    private String maxPollRecords;

    @Value("${kafka.consumer.fetch.max.wait.ms:500}")
    private String fetchMaxWaitMs;

    @Value("${kafka.consumer.request.timeout.ms:60000}")
    private String requestTimeoutMs;

    @Value("${kafka.consumer.heartbeat.interval.ms:3000}")
    private String heartbeatIntervalMs;

    @Value("${kafka.topic.payments.dlq}")
    private String dlqTopic;

    @Bean
    @ConditionalOnProperty(name = "kafka.config.external-properties.enabled", havingValue = "true", matchIfMissing = true)
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

        Resource[] resources = new Resource[]{
                new ClassPathResource("kafka.properties"),
        };

        configurer.setLocations(resources);
        configurer.setIgnoreResourceNotFound(true);
        configurer.setLocalOverride(true);

        return configurer;
    }

    @Bean("paymentRequestConsumerFactory")
    public ConsumerFactory<String, PaymentRequestEvent> paymentRequestConsumerFactory() {
        Map<String, Object> props = getKafkaProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, paymentRequestConsumerGroup);

        JacksonJsonDeserializer<PaymentRequestEvent> deserializer = new JacksonJsonDeserializer<>(PaymentRequestEvent.class);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }

    @Bean("paymentRequestListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestEvent> paymentRequestListenerContainerFactory(
            @Qualifier("paymentRequestConsumerFactory") ConsumerFactory<String, PaymentRequestEvent> consumerFactory,
            @Qualifier("kafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentRequestEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    logger.error("Sending message to DLQ. Topic: {}, Key: {}, Exception: {}",
                            record.topic(), record.key(), exception.getMessage());
                    return new TopicPartition(dlqTopic, record.partition());
                }
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(0, 0)
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    private Map<String, Object> getKafkaProperties() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs);

        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);

        return props;
    }
}