package com.iprody.crm.inventoryservice.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GroupFilter {
    private UUID groupRefId;
    private Boolean isAvailFreePlaces;
}
