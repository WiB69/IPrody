package com.iprody.crm.inquiryservice.model.dto;

import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class InquiryUpdateData {
    private InquiryStatus status;
    private UUID managerRefId;
}
