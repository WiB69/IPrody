package com.iprody.crm.paymentservice;

import com.iprody.crm.paymentservice.config.SpringBootApplicationTest;
import com.iprody.crm.paymentservice.model.entity.Payment;
import com.iprody.crm.paymentservice.model.enums.PaymentStatus;
import com.iprody.crm.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
class PaymentRepositoryTest extends SpringBootApplicationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private final UUID testInquiryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        Payment p1 = new Payment();
        p1.setInquiryRefId(testInquiryId);
        p1.setAmount(new BigDecimal("100.00"));
        p1.setCurrency("USD");
        p1.setPaymentStatus(PaymentStatus.PENDING);
        p1.setCreatedAt(Timestamp.valueOf("2024-01-01 10:00:00"));
        p1.setUpdatedAt(p1.getCreatedAt());

        Payment p2 = new Payment();
        p2.setInquiryRefId(UUID.randomUUID());
        p2.setAmount(new BigDecimal("200.00"));
        p2.setCurrency("EUR");
        p2.setPaymentStatus(PaymentStatus.APPROVED);
        p2.setCreatedAt(Timestamp.valueOf("2024-02-01 10:00:00"));
        p2.setUpdatedAt(p2.getCreatedAt());

        paymentRepository.saveAll(List.of(p1, p2));
    }

    @Test
    void shouldFindByDateRange() {
        Timestamp from = Timestamp.valueOf("2023-12-31 23:59:59");
        Timestamp to = Timestamp.valueOf("2024-01-15 00:00:00");

        Page<Payment> result = paymentRepository.findAllByFilter(
                from, to, null, null, null, PageRequest.of(0, 10));

        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getCurrency()).isEqualTo("USD"),
                () -> assertThat(result.getContent().get(0).getAmount()).isEqualTo(new BigDecimal("100.00")),
                () -> assertThat(result.getContent().get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PENDING)
        );
    }

    @Test
    void shouldFindByStatusAndInquiry() {
        Page<Payment> result = paymentRepository.findAllByFilter(
                null, null, null, testInquiryId, PaymentStatus.PENDING, PageRequest.of(0, 10));

        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getInquiryRefId()).isEqualTo(testInquiryId),
                () -> assertThat(result.getContent().get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PENDING),
                () -> assertThat(result.getContent().get(0).getAmount()).isEqualTo(new BigDecimal("100.00"))
        );
    }

    @Test
    void shouldReturnAllWhenFiltersAreNull() {
        Page<Payment> result = paymentRepository.findAllByFilter(
                null, null, null, null, null, PageRequest.of(0, 10));

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent()).extracting(Payment::getCurrency)
                        .containsExactlyInAnyOrder("USD", "EUR"),
                () -> assertThat(result.getContent()).extracting(Payment::getPaymentStatus)
                        .containsExactlyInAnyOrder(PaymentStatus.PENDING, PaymentStatus.APPROVED)
        );
    }

    @Test
    void shouldReturnEmptyWhenNoMatches() {
        Timestamp from = Timestamp.valueOf("2025-01-01 00:00:00");
        Timestamp to = Timestamp.valueOf("2025-12-31 23:59:59");

        Page<Payment> result = paymentRepository.findAllByFilter(
                from, to, null, null, null, PageRequest.of(0, 10));

        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isZero(),
                () -> assertThat(result.getTotalPages()).isZero()
        );
    }
}
