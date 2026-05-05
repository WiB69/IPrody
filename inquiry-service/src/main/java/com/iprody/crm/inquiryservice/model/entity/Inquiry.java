package com.iprody.crm.inquiryservice.model.entity;

import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table(name = "inquiry")
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_ref_id", nullable = false)
    private UUID productRefId;

    @Column(name = "customer_ref_id", nullable = false)
    private UUID customerRefId;

    @Column(name = "group_ref_id")
    private UUID groupRefId;

    @Column(name = "manager_ref_id", nullable = false)
    private UUID managerRefId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "comment")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InquiryStatus status;

    @Column(name = "note")
    private String note;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Generated(event = EventType.INSERT)
    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}
