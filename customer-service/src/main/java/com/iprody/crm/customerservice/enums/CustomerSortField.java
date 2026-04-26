package com.iprody.crm.customerservice.enums;

import lombok.Getter;

@Getter
public enum CustomerSortField {
    FULL_NAME("fullName");

    private final String fieldName;

    CustomerSortField(String name) {
        this.fieldName = name;
    }

}
