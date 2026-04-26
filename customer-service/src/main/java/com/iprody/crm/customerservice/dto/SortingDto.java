package com.iprody.crm.customerservice.dto;

import com.iprody.crm.customerservice.enums.CustomerSortField;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class SortingDto {
    private CustomerSortField sortField;
    private Sort.Direction sortDirection;
}
