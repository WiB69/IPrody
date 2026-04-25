package com.iprody.crm.inquiryservice.model.dto;

import com.iprody.crm.inquiryservice.enums.InquirySortField;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class SortingDto {
    private InquirySortField sortField;
    private Sort.Direction sortDirection;
}
