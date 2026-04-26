package com.iprody.crm.customerservice;

import com.iprody.crm.customerservice.dto.*;
import com.iprody.crm.customerservice.entity.Contract;
import com.iprody.crm.customerservice.entity.Customer;
import com.iprody.crm.customerservice.enums.CustomerSortField;
import com.iprody.crm.customerservice.repository.ContractRepository;
import com.iprody.crm.customerservice.repository.CustomerRepository;
import com.iprody.crm.customerservice.service.CustomerServiceImpl;
import com.iprody.crm.customerservice.utils.Sorting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private UUID testCustomerId;
    private Customer testCustomer;
    private CustomerData testCustomerData;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        Contract testContract = new Contract();
        testContract.setId(UUID.randomUUID());
        testContract.setEmail("test@example.com");
        testContract.setPhoneNumber("+1234567890");
        testContract.setCreatedAt(now);
        testContract.setUpdatedAt(now);

        testCustomer = new Customer();
        testCustomer.setId(testCustomerId);
        testCustomer.setFullName("Test Customer");
        testCustomer.setContract(testContract);
        testCustomer.setCreatedAt(now);
        testCustomer.setUpdatedAt(now);

        ContractData contractData = new ContractData();
        contractData.setEmail("test@example.com");
        contractData.setPhoneNumber("+1234567890");

        testCustomerData = new CustomerData();
        testCustomerData.setFullName("Test Customer");
        testCustomerData.setContract(contractData);
    }

    @Test
    void shouldSaveCustomer() {
        Contract savedContract = new Contract();
        savedContract.setId(UUID.randomUUID());
        savedContract.setEmail("test@example.com");
        savedContract.setPhoneNumber("+1234567890");

        when(contractRepository.save(any(Contract.class))).thenReturn(savedContract);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        Customer result = customerService.save(testCustomerData);

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getId()).isEqualTo(testCustomerId),
                () -> assertThat(result.getFullName()).isEqualTo("Test Customer"),
                () -> verify(contractRepository, times(1)).save(any(Contract.class)),
                () -> verify(customerRepository, times(1)).save(any(Customer.class))
        );
    }

    @Test
    void shouldFindById() {
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));

        Customer result = customerService.findById(testCustomerId);

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getId()).isEqualTo(testCustomerId),
                () -> assertThat(result.getFullName()).isEqualTo("Test Customer"),
                () -> verify(customerRepository, times(1)).findById(testCustomerId)
        );
    }

    @Test
    void shouldReturnNullWhenCustomerNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Customer result = customerService.findById(nonExistentId);

        assertThat(result).isNull();
        verify(customerRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void shouldFindAllByFilter() {
        CustomerFilter filter = new CustomerFilter();
        filter.setFullName("Test");

        Sorting sorting = new Sorting(CustomerSortField.FULL_NAME, Sort.Direction.DESC);

        Page<Customer> mockPage = new PageImpl<>(List.of(testCustomer));

        when(customerRepository.findAllByFullName(eq("Test"), any(PageRequest.class)))
                .thenReturn(mockPage);

        List<Customer> result = customerService.findAllByFilter(filter, 0, 10, sorting);

        assertAll(
                () -> assertThat(result.isEmpty()).isFalse(),
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.get(0).getId()).isEqualTo(testCustomerId),
                () -> verify(customerRepository, times(1)).findAllByFullName(eq("Test"), any(PageRequest.class))
        );
    }

    @Test
    void shouldFindAllByFilterWithEmptyResult() {
        CustomerFilter filter = new CustomerFilter();
        filter.setFullName("NonExistent");

        Sorting sorting = new Sorting(CustomerSortField.FULL_NAME, Sort.Direction.DESC);

        Page<Customer> emptyPage = new PageImpl<>(List.of());

        when(customerRepository.findAllByFullName(eq("NonExistent"), any(PageRequest.class)))
                .thenReturn(emptyPage);

        List<Customer> result = customerService.findAllByFilter(filter, 0, 10, sorting);

        assertAll(
                () -> assertThat(result.isEmpty()).isTrue(),
                () -> verify(customerRepository, times(1)).findAllByFullName(eq("NonExistent"), any(PageRequest.class))
        );
    }

    @Test
    void shouldFindAllByFilterWithNullFullName() {
        CustomerFilter filter = new CustomerFilter();
        filter.setFullName(null);

        Sorting sorting = new Sorting(CustomerSortField.FULL_NAME, Sort.Direction.ASC);

        Page<Customer> mockPage = new PageImpl<>(List.of(testCustomer));

        when(customerRepository.findAllByFullName(eq(null), any(PageRequest.class)))
                .thenReturn(mockPage);

        List<Customer> result = customerService.findAllByFilter(filter, 0, 10, sorting);

        assertThat(result).isNotNull();
        verify(customerRepository, times(1)).findAllByFullName(eq(null), any(PageRequest.class));
    }

    @Test
    void shouldUpdateCustomer() {
        CustomerData updateData = new CustomerData();
        ContractData contractData = new ContractData();
        contractData.setEmail("updated@example.com");
        contractData.setPhoneNumber("+9999999999");
        updateData.setFullName("Updated Customer");
        updateData.setContract(contractData);

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.update(testCustomerId, updateData);

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getFullName()).isEqualTo("Updated Customer"),
                () -> assertThat(result.getContract().getEmail()).isEqualTo("updated@example.com"),
                () -> assertThat(result.getContract().getPhoneNumber()).isEqualTo("+9999999999"),
                () -> verify(customerRepository, times(1)).findById(testCustomerId),
                () -> verify(customerRepository, times(1)).save(any(Customer.class))
        );
    }

    @Test
    void shouldDeleteCustomer() {
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        doNothing().when(customerRepository).delete(any(Customer.class));

        customerService.delete(testCustomerId);

        verify(customerRepository, times(1)).findById(testCustomerId);
        verify(customerRepository, times(1)).delete(testCustomer);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
        UUID nonExistentId = UUID.randomUUID();

        when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        customerService.delete(nonExistentId);

        verify(customerRepository, times(1)).findById(nonExistentId);
        verify(customerRepository, never()).delete(any(Customer.class));
    }
}
