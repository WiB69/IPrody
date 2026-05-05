package com.iprody.crm.inquiryservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class InquiryDataDto {
    @NotNull
    private UUID productRefId;
    @NotNull
    private UUID customerRefId;
    @NotNull
    private UUID managerRefId;
    @NotBlank
    private String source;
}
