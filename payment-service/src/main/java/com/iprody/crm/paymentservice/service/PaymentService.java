package com.iprody.crm.paymentservice.service;

import com.iprody.crm.paymentservice.model.dto.PaymentFilter;
import com.iprody.crm.paymentservice.model.entity.Payment;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<Payment> findAllByFilter(PaymentFilter paymentFilter, Pageable pageParams);

    boolean existsByInquiryRefId(UUID orderRef);
}