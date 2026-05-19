package com.iprody.crm.paymentservice.repository;

import com.iprody.crm.paymentservice.model.entity.OutboxMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    @Query("SELECT o FROM OutboxMessage o WHERE" +
            " o.type = :type AND" +
            " o.status = :status AND" +
            " o.retryCount < :maxRetryCount" +
            " ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingMessages(
            @Param("type") OutboxMessage.OutboxType type,
            @Param("status") OutboxMessage.OutboxStatus status,
            @Param("maxRetryCount") int maxRetryCount,
            Pageable pageable
    );
}
