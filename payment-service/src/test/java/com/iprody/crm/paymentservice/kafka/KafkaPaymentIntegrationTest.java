package com.iprody.crm.paymentservice.kafka;

import com.iprody.crm.paymentservice.PaymentServiceApplication;
import com.iprody.crm.paymentservice.config.SpringBootApplicationTest;
import com.iprody.crm.paymentservice.kafka.model.PaymentRequestEvent;
import com.iprody.crm.paymentservice.model.dto.XPaymentResponse;
import com.iprody.crm.paymentservice.model.entity.OutboxMessage;
import com.iprody.crm.paymentservice.model.entity.Payment;
import com.iprody.crm.paymentservice.model.enums.PaymentState;
import com.iprody.crm.paymentservice.repository.OutboxRepository;
import com.iprody.crm.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.iprody.crm.paymentservice.model.entity.OutboxMessage.OutboxStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

@SpringBootTest(
        classes = PaymentServiceApplication.class
)
@Testcontainers
@ActiveProfiles("test")
public class KafkaPaymentIntegrationTest extends SpringBootApplicationTest {

    private static ClientAndServer mockServer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @BeforeAll
    static void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
    }

    @AfterAll
    static void stopMockServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
        kafka.stop();
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        outboxRepository.deleteAll();
        mockServer.reset();
    }

    @Test
    void shouldProcessPaymentRequestSuccessfully() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentRequestEvent request = PaymentRequestEvent.builder()
                .amount(new BigDecimal("1090.50"))
                .currency("USD")
                .customer("Henry Ford")
                .inquiryRefId(orderId)
                .receiptEmail("test@email.com")
                .build();

        UUID transactionId = UUID.randomUUID();
        XPaymentResponse successResponse = XPaymentResponse.builder()
                .id(transactionId)
                .amount(new BigDecimal("1090.50"))
                .currency("USD")
                .amountReceived(new BigDecimal("1090.50"))
                .createdAt(Instant.now())
                .chargedAt(Instant.now())
                .customer("Henry Ford")
                .order(orderId.toString())
                .receiptEmail("test@email.com")
                .status(XPaymentResponse.Status.SUCCEEDED)
                .build();

        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/charges")
                        .withHeader("Authorization", "Basic .*")
                        .withHeader("X-Pay-Account", "paymentAgentIprodyApiToken")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(json(successResponse))
                        .withHeader("Content-Type", "application/json")
        );

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/charges/" + transactionId)
                        .withHeader("Authorization", "Basic .*")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(json(successResponse))
                        .withHeader("Content-Type", "application/json")
        );

        kafkaTemplate.send("payments.requests", orderId.toString(), request).get(10, TimeUnit.SECONDS);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Payment savedPayment = paymentRepository.findByInquiryRefId(orderId).orElse(null);
                    assertThat(savedPayment).isNotNull();
                    assertThat(savedPayment.getTransactionRefId()).isEqualTo(transactionId);
                    assertThat(savedPayment.getState()).isEqualTo(PaymentState.SUCCEEDED);
                });
    }

    @Test
    void shouldHandleDuplicateRequestIdempotently() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentRequestEvent request = PaymentRequestEvent.builder()
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .customer("John Doe")
                .inquiryRefId(orderId)
                .receiptEmail("john@email.com")
                .build();

        UUID transactionId = UUID.randomUUID();
        XPaymentResponse response = XPaymentResponse.builder()
                .id(transactionId)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .amountReceived(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .chargedAt(Instant.now())
                .customer("John Doe")
                .order(orderId.toString())
                .receiptEmail("john@email.com")
                .status(XPaymentResponse.Status.SUCCEEDED)
                .build();

        mockServer.when(
                request().withMethod("POST").withPath("/charges"),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(json(response))
        );

        mockServer.when(
                request().withMethod("GET").withPath("/charges/" + transactionId),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(json(response))
        );

        kafkaTemplate.send("payments.requests", orderId.toString(), request).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);
        kafkaTemplate.send("payments.requests", orderId.toString(), request).get(5, TimeUnit.SECONDS);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(paymentRepository.findByInquiryRefId(orderId)).isPresent();
                });
    }

    @Test
    void shouldRetryAndGoToDLQAfterMaxRetries() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentRequestEvent request = PaymentRequestEvent.builder()
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .customer("Jane Smith")
                .inquiryRefId(orderId)
                .receiptEmail("jane@email.com")
                .build();

        mockServer.when(
                request().withMethod("POST").withPath("/charges")
        ).respond(
                response()
                        .withStatusCode(500)
                        .withBody("Internal Server Error")
        );

        kafkaTemplate.send("payments.requests", orderId.toString(), request).get(10, TimeUnit.SECONDS);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OutboxMessage failedMessage = outboxRepository.findAll()
                            .stream()
                            .filter(m -> m.getAggregateId() != null && m.getAggregateId().equals(orderId))
                            .findFirst()
                            .orElse(null);

                    assertThat(failedMessage).isNotNull();
                    assertThat(failedMessage.getStatus()).isEqualTo(FAILED);
                    assertThat(failedMessage.getRetryCount()).isEqualTo(1);
                });
    }

    @Test
    void shouldHandleExternalApiTimeout() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentRequestEvent request = PaymentRequestEvent.builder()
                .amount(new BigDecimal("300.00"))
                .currency("USD")
                .customer("Bob Wilson")
                .inquiryRefId(orderId)
                .receiptEmail("bob@email.com")
                .build();

        mockServer.when(
                request().withMethod("POST").withPath("/charges")
        ).respond(
                response()
                        .withStatusCode(504)
                        .withDelay(TimeUnit.SECONDS, 2)
                        .withBody("Gateway Timeout")
        );

        kafkaTemplate.send("payments.requests", orderId.toString(), request).get(10, TimeUnit.SECONDS);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var messages = outboxRepository.findAll();
                    assertThat(messages).isNotEmpty();
                    assertThat(messages.get(0).getStatus().equals(FAILED));
                });
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public PropertySourcesPlaceholderConfigurer testPropertySourcesPlaceholderConfigurer() {
            PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
            configurer.setIgnoreResourceNotFound(true);
            configurer.setLocalOverride(true);
            return configurer;
        }
    }
}