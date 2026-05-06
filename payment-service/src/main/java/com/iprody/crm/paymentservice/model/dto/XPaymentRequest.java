package com.iprody.crm.paymentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XPaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String customer;
    private String order;
    private String receiptEmail;
}