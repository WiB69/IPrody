package com.iprody.crm.inquiryservice.model.dto;

import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class InquiryFilterDto {
    private InquiryStatus status;
    private UUID productRefId;
    private UUID customerRefId;
    private UUID managerRefId;
}
