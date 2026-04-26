package com.iprody.crm.customerservice.service;

import com.iprody.crm.customerservice.dto.CustomerData;
import com.iprody.crm.customerservice.dto.CustomerFilter;
import com.iprody.crm.customerservice.entity.Customer;
import com.iprody.crm.customerservice.utils.Sorting;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    Customer save(CustomerData customerData);

    Customer findById(UUID id);

    List<Customer> findAllByFilter(CustomerFilter filter, Integer offset, Integer limit, Sorting sorting);

    Customer update(UUID id, CustomerData customerData);

    void delete(UUID id);
}
