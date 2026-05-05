package com.iprody.crm.customerservice.service;

import com.iprody.crm.customerservice.dto.ContractData;
import com.iprody.crm.customerservice.dto.CustomerData;
import com.iprody.crm.customerservice.dto.CustomerFilter;
import com.iprody.crm.customerservice.entity.Contract;
import com.iprody.crm.customerservice.entity.Customer;
import com.iprody.crm.customerservice.exception.InvalidRequestException;
import com.iprody.crm.customerservice.exception.ResourceNotFoundException;
import com.iprody.crm.customerservice.repository.ContractRepository;
import com.iprody.crm.customerservice.repository.CustomerRepository;
import com.iprody.crm.customerservice.utils.Sorting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ContractRepository contractRepository;

    @Transactional
    public Customer save(CustomerData customerData) {
        log.debug("Saving new customer with data: {}", customerData);
        if (customerData.getContract() == null) {
            throw new InvalidRequestException("Contract data is required");
        }

        ContractData contractData = customerData.getContract();
        Contract contractSaved = contractRepository.save(new Contract(contractData.getEmail(), contractData.getPhoneNumber()));
        return customerRepository.save(new Customer(customerData.getFullName(), contractSaved));
    }

    @Transactional(readOnly = true)
    public Customer findById(UUID id) {
        log.debug("Finding customer by id: {}", id);
        return customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer not found with id: {}", id);
                    return new ResourceNotFoundException("Customer not found with id: " + id);
                });
    }

    public List<Customer> findAllByFilter(CustomerFilter filter, Integer offset, Integer limit, Sorting sorting) {
        return customerRepository.findAllByFullName(
                filter.getFullName(),
                PageRequest.of(
                        offset,
                        limit,
                        Sort.by(sorting.getSortDirection(), sorting.getSortField().getFieldName())))
                .getContent();
    }

    @Transactional
    public Customer update(UUID id, CustomerData customerData) {
        log.debug("Updating customer with id: {}", id);

        Customer customerDb = findById(id);
        customerDb.setFullName(customerData.getFullName());
        customerDb.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        customerDb.getContract().setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        customerDb.getContract().setEmail(customerData.getContract().getEmail());
        customerDb.getContract().setPhoneNumber(customerData.getContract().getPhoneNumber());
        return customerRepository.save(customerDb);
    }

    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting customer with id: {}", id);
        customerRepository.delete(findById(id));
    }
}
