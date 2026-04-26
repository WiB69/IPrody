package com.iprody.crm.customerservice;

import com.iprody.crm.customerservice.config.SpringBootApplicationTest;
import com.iprody.crm.customerservice.entity.Contract;
import com.iprody.crm.customerservice.entity.Customer;
import com.iprody.crm.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
class CustomerRepositoryTest extends SpringBootApplicationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        Contract contract1 = new Contract("john@example.com", "+1234567890");
        Contract contract2 = new Contract("jane@example.com", "+0987654321");
        Contract contract3 = new Contract("johnson@example.com", "+1122334455");
        Contract contract4 = new Contract("alice@example.com", "+5544332211");

        Customer customer1 = new Customer("John Doe", contract1);
        Customer customer2 = new Customer("Jane Smith", contract2);
        Customer customer3 = new Customer("John Johnson", contract3);
        Customer customer4 = new Customer("Alice Brown", contract4);

        customerRepository.saveAll(List.of(customer1, customer2, customer3, customer4));
    }

    @Test
    void shouldFindByFullNameExactMatch() {
        Page<Customer> result = customerRepository.findAllByFullName(
                "John Doe",
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe"),
                () -> assertThat(result.getContent().get(0).getContract().getEmail()).isEqualTo("john@example.com")
        );
    }

    @Test
    void shouldReturnEmptyWhenNoMatches() {
        Page<Customer> result = customerRepository.findAllByFullName(
                "NonExistentName",
                PageRequest.of(0, 10)
        );

        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isZero(),
                () -> assertThat(result.getTotalPages()).isZero()
        );
    }

    @Test
    void shouldFailToSaveWhenEmailIsNull() {
        Contract invalidContract = new Contract(null, "+79991234567");
        Customer invalidCustomer = new Customer("Test Customer", invalidContract);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(invalidCustomer))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("null value in column \"email\"")
                .hasMessageContaining("violates not-null constraint");
    }

    @Test
    void shouldFailToSaveWhenContractIsNull() {
        Customer invalidCustomer = new Customer("Test Customer", null);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(invalidCustomer))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("null value in column \"contract_details_is\"");
    }

    @Test
    void shouldSupportSorting() {
        Page<Customer> descResult = customerRepository.findAllByFullName(
                "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "fullName"))
        );

        Page<Customer> ascResult = customerRepository.findAllByFullName(
                "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "fullName"))
        );

        assertAll(
                () -> assertThat(descResult.getContent())
                        .extracting(Customer::getFullName)
                        .isSortedAccordingTo(Comparator.reverseOrder()),
                () -> assertThat(ascResult.getContent())
                        .extracting(Customer::getFullName)
                        .isSortedAccordingTo(Comparator.naturalOrder())
        );
    }

    @Test
    void shouldSupportSortingByCreatedAt() {
        Page<Customer> descResult = customerRepository.findAllByFullName(
                "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Page<Customer> ascResult = customerRepository.findAllByFullName(
                "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"))
        );

        assertThat(descResult.getContent())
                .extracting(Customer::getCreatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());

        assertThat(ascResult.getContent())
                .extracting(Customer::getCreatedAt)
                .isSortedAccordingTo(Comparator.naturalOrder());
    }
}