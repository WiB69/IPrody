package com.iprody.crm.inquiryservice.utils;

import com.iprody.crm.inquiryservice.enums.InquirySortField;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
public class Sorting {
    private InquirySortField sortField;
    private Sort.Direction sortDirection;
}
