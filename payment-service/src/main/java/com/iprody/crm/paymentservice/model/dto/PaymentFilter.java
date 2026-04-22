package com.iprody.crm.paymentservice.model.dto;

import com.iprody.crm.paymentservice.model.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
public class PaymentFilter {
    private UUID id;
    private UUID inquiryRefId;
    private BigDecimal amount;
    private PaymentStatus status;
    private Timestamp from;
    private Timestamp to;

}
