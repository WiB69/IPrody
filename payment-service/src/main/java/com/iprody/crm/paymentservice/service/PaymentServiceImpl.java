package com.iprody.crm.paymentservice.service;

import com.iprody.crm.paymentservice.model.dto.PaymentFilter;
import com.iprody.crm.paymentservice.model.entity.Payment;
import com.iprody.crm.paymentservice.repository.PaymentRepository;
import com.iprody.crm.paymentservice.utils.PaymentDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDataValidator pageDataValidator;

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAllByFilter(PaymentFilter paymentFilter, Pageable pageParams) {
        Pageable pageable = pageDataValidator.validatePageParamsAndGetValid(
                pageParams.getPageNumber(),
                pageParams.getPageSize()
        );

        return paymentRepository.findAllByFilter(
                paymentFilter.getFrom(),
                paymentFilter.getTo(),
                paymentFilter.getId(),
                paymentFilter.getInquiryRefId(),
                paymentFilter.getStatus(),
                pageable).getContent();
    }

    @Override
    public boolean existsByInquiryRefId(UUID orderRef) {
        return paymentRepository.existsByInquiryRefId(orderRef);
    }
}