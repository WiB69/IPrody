package com.iprody.crm.inquiryservice.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class InquiryData {
    private UUID productRefId;
    private UUID customerRefId;
    private UUID managerRefId;
    private String source;
}
