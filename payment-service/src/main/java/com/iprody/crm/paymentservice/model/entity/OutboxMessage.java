package com.iprody.crm.paymentservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OUTBOX_MESSAGE", schema = "payment_db")
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "PAYMENT_ID")
    private UUID paymentId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "EVENT", nullable = false, columnDefinition = "jsonb")
    private String event;

    @Column(name = "AGGREGATE_ID")
    private UUID aggregateId;

    @Column(name = "TRANSACTION_ID")
    private UUID transactionId;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "RETRY_COUNT", nullable = false)
    private Integer retryCount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private OutboxType type;

    public enum OutboxStatus {
        PENDING, PROCESSED, FAILED
    }
    public enum OutboxType {
        INCOMING_PAYMENT,
        STATUS_CHECK
    }
}