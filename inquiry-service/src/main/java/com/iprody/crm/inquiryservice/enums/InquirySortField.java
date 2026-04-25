package com.iprody.crm.inquiryservice.enums;

public enum InquirySortField {
    STATUS("status"),
    CREATED_AT("createdAt"),;

    private final String fieldName;

    InquirySortField(final String fieldName) {
        this.fieldName = fieldName;
    }
    public String getFieldName() {
        return fieldName;
    }
}