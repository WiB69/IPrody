package com.iprody.crm.inquiryservice.repository;

import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.UUID;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {

    @Query("""
            select inq from Inquiry inq
            where(:status is null or inq.status = :status)
            and (:customerRefId is null or inq.customerRefId = :customerRefId)
            and (:managerRefId is null or inq.managerRefId = :managerRefId)
            """)
    Page<Inquiry> findAllByFilter(
            @Param("status") InquiryStatus status,
            @Param("customerRefId") UUID customerRefId,
            @Param("managerRefId") UUID managerRefId,
            Pageable pageable
    );
}
