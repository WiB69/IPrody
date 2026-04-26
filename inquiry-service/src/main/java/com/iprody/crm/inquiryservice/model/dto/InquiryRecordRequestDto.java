package com.iprody.crm.inquiryservice.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InquiryRecordRequestDto {
    private InquiryFilterDto filter;
    private SortingDto sortDto;

    @Min(value = 0, message = "Offset must be non-negative")
    private Integer offset = 0;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    private Integer limit = 10;
}
