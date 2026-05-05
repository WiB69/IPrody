package com.iprody.crm.inquiryservice.model.dto;

import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class InquiryFilter {
    private InquiryStatus status;
    private UUID customerRefId;
    private UUID managerRefId;
}
