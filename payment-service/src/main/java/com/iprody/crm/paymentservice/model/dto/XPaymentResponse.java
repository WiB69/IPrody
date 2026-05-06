package com.iprody.crm.paymentservice.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XPaymentResponse {

    private UUID id;
    private BigDecimal amount;
    private String currency;
    private BigDecimal amountReceived;
    private Instant createdAt;
    private Instant chargedAt;
    private String customer;
    private String order;
    private String receiptEmail;
    private Status status;

    public enum Status {
        PROCESSING,
        CANCELED,
        SUCCEEDED;

        @JsonCreator
        public static Status fromString(String value) {
            if (value == null) return null;

            return switch (value.toLowerCase()) {
                case "processing" -> PROCESSING;
                case "canceled" -> CANCELED;
                case "succeeded" -> SUCCEEDED;
                default -> throw new IllegalArgumentException("Unknown status: " + value);
            };
        }
    }
}