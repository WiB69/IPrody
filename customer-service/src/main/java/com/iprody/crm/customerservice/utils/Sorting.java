package com.iprody.crm.customerservice.utils;

import com.iprody.crm.customerservice.enums.CustomerSortField;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
public class Sorting {
    private CustomerSortField sortField;
    private Sort.Direction sortDirection;
}
