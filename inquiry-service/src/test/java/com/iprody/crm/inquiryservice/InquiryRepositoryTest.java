package com.iprody.crm.inquiryservice;

import com.iprody.crm.inquiryservice.config.SpringBootApplicationTest;
import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import com.iprody.crm.inquiryservice.repository.InquiryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
class InquiryRepositoryTest extends SpringBootApplicationTest {

    @Autowired
    private InquiryRepository inquiryRepository;

    private final UUID testCustomerRefId = UUID.randomUUID();
    private final UUID testManagerRefId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        inquiryRepository.deleteAll();

        Inquiry inquiry1 = new Inquiry();
        inquiry1.setProductRefId(UUID.randomUUID());
        inquiry1.setCustomerRefId(testCustomerRefId);
        inquiry1.setManagerRefId(testManagerRefId);
        inquiry1.setSource("WEB");
        inquiry1.setStatus(InquiryStatus.NEW);
        inquiry1.setComment("First test inquiry");
        inquiry1.setUpdatedAt(inquiry1.getCreatedAt());

        Inquiry inquiry2 = new Inquiry();
        inquiry2.setProductRefId(UUID.randomUUID());
        inquiry2.setCustomerRefId(UUID.randomUUID());
        inquiry2.setManagerRefId(testManagerRefId);
        inquiry2.setSource("MOBILE");
        inquiry2.setStatus(InquiryStatus.IN_PROGRESS);
        inquiry2.setComment("Second test inquiry");
        inquiry2.setUpdatedAt(inquiry2.getCreatedAt());

        Inquiry inquiry3 = new Inquiry();
        inquiry3.setProductRefId(UUID.randomUUID());
        inquiry3.setCustomerRefId(testCustomerRefId);
        inquiry3.setManagerRefId(UUID.randomUUID());
        inquiry3.setSource("API");
        inquiry3.setStatus(InquiryStatus.PAID);
        inquiry3.setComment("Third test inquiry");
        inquiry3.setUpdatedAt(inquiry3.getCreatedAt());

        inquiryRepository.saveAll(List.of(inquiry1, inquiry2, inquiry3));
    }

    @Test
    void shouldFindByStatus() {
        Page<Inquiry> result = inquiryRepository.findAllByFilter(
                InquiryStatus.NEW,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getStatus()).isEqualTo(InquiryStatus.NEW),
                () -> assertThat(result.getContent().get(0).getSource()).isEqualTo("WEB")
        );
    }

    @Test
    void shouldFindByCustomerRefId() {
        Page<Inquiry> result = inquiryRepository.findAllByFilter(
                null,
                testCustomerRefId,
                null,
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent())
                        .allMatch(inquiry -> inquiry.getCustomerRefId().equals(testCustomerRefId)),
                () -> assertThat(result.getContent())
                        .extracting(Inquiry::getStatus)
                        .containsExactlyInAnyOrder(InquiryStatus.NEW, InquiryStatus.PAID)
        );
    }

    @Test
    void shouldFindByManagerRefId() {
        Page<Inquiry> result = inquiryRepository.findAllByFilter(
                null,
                null,
                testManagerRefId,
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent())
                        .allMatch(inquiry -> inquiry.getManagerRefId().equals(testManagerRefId)),
                () -> assertThat(result.getContent())
                        .extracting(Inquiry::getStatus)
                        .containsExactlyInAnyOrder(InquiryStatus.NEW, InquiryStatus.IN_PROGRESS)
        );
    }

    @Test
    void shouldFindByStatusAndCustomerAndManager() {
        Page<Inquiry> result = inquiryRepository.findAllByFilter(
                InquiryStatus.NEW,
                testCustomerRefId,
                testManagerRefId,
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getStatus()).isEqualTo(InquiryStatus.NEW),
                () -> assertThat(result.getContent().get(0).getCustomerRefId()).isEqualTo(testCustomerRefId),
                () -> assertThat(result.getContent().get(0).getManagerRefId()).isEqualTo(testManagerRefId)
        );
    }

    @Test
    void shouldReturnAllWhenFiltersAreNull() {
        Page<Inquiry> result = inquiryRepository.findAllByFilter(
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(3),
                () -> assertThat(result.getContent()).hasSize(3),
                () -> assertThat(result.getContent()).extracting(Inquiry::getStatus)
                        .containsExactlyInAnyOrder(InquiryStatus.NEW, InquiryStatus.IN_PROGRESS, InquiryStatus.PAID),
                () -> assertThat(result.getContent()).extracting(Inquiry::getSource)
                        .containsExactlyInAnyOrder("WEB", "MOBILE", "API")
        );
    }

    @Test
    void shouldReturnEmptyWhenNoMatches() {
        UUID nonExistentCustomerId = UUID.randomUUID();

        Page<Inquiry> result = inquiryRepository.findAllByFilter(
                InquiryStatus.REJECTED,
                nonExistentCustomerId,
                null,
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isZero(),
                () -> assertThat(result.getTotalPages()).isZero()
        );
    }

    @Test
    void shouldSupportPagination() {
        Page<Inquiry> firstPage = inquiryRepository.findAllByFilter(
                null,
                null,
                null,
                PageRequest.of(0, 2)
        );

        Page<Inquiry> secondPage = inquiryRepository.findAllByFilter(
                null,
                null,
                null,
                PageRequest.of(1, 2)
        );

        assertAll(
                () -> assertThat(firstPage.getContent()).hasSize(2),
                () -> assertThat(secondPage.getContent()).hasSize(1),
                () -> assertThat(firstPage.getTotalElements()).isEqualTo(3),
                () -> assertThat(firstPage.getTotalPages()).isEqualTo(2)
        );
    }

    @Test
    void shouldSupportSorting() {
        Page<Inquiry> descResult = inquiryRepository.findAllByFilter(
                null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Page<Inquiry> ascResult = inquiryRepository.findAllByFilter(
                null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"))
        );

        assertThat(descResult.getContent())
                .extracting(Inquiry::getCreatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());

        assertThat(ascResult.getContent())
                .extracting(Inquiry::getCreatedAt)
                .isSortedAccordingTo(Comparator.naturalOrder());
    }
}
