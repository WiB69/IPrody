package com.iprody.crm.customerservice.enums;

public enum CustomerSortField {
    FULL_NAME("fullName");

    private final String fieldName;

    CustomerSortField(String name) {
        this.fieldName = name;
    }

    public String getFieldName() {
        return fieldName;
    }
}
