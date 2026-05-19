package com.iprody.crm.paymentservice.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public class SpringBootApplicationTest {

    protected static final int MOCK_SERVER_PORT = 1080;

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("payment_db")
            .withReuse(true);

    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("kafka.config.external-properties.enabled", () -> "false");
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:/db/changelog/main-changelog.yaml");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "payment_db");

        registry.add("xpayment.api.url", () -> "http://localhost:" + MOCK_SERVER_PORT);
        registry.add("xpayment.api.username", () -> "paymentAgentIprody");
        registry.add("xpayment.api.password", () -> "iprodyTestPassword0123");
        registry.add("xpayment.api.account-header", () -> "paymentAgentIprodyApiToken");

        registry.add("kafka.bootstrap.servers", kafka::getBootstrapServers);
        registry.add("kafka.consumer.payment.request.group", () -> "test-group");

        registry.add("kafka.topic.payments.requests", () -> "payments.requests");
        registry.add("kafka.topic.payments.response", () -> "payments.response");
        registry.add("kafka.topic.payments.dlq", () -> "payments.dlq");

        registry.add("outbox.scheduler.enabled", () -> "true");
        registry.add("outbox.scheduler.fixed-delay", () -> "500");
        registry.add("outbox.scheduler.status-check-delay", () -> "500");
        registry.add("outbox.batch-size", () -> "5");
        registry.add("outbox.max-retry-count", () -> "1");

        registry.add("kafka.consumer.auto.offset.reset", () -> "earliest");
        registry.add("kafka.consumer.enable.auto.commit", () -> "false");
        registry.add("kafka.consumer.max.poll.records", () -> "500");
        registry.add("kafka.consumer.fetch.max.wait.ms", () -> "500");
    }
}
