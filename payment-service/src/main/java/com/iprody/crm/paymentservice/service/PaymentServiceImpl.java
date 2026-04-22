package com.iprody.crm.paymentservice.service;

import com.iprody.crm.paymentservice.model.dto.PaymentFilter;
import com.iprody.crm.paymentservice.model.entity.Payment;
import com.iprody.crm.paymentservice.repository.PaymentRepository;
import com.iprody.crm.paymentservice.utils.PaymentDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDataValidator pageDataValidator;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentDataValidator pageDataValidator) {
        this.paymentRepository = paymentRepository;
        this.pageDataValidator = pageDataValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAllByFilter(PaymentFilter paymentFilter,
                                         Pageable pageParams) {
        //TODO: move validation after KafkaListener realization
        Pageable pageable = pageDataValidator.validatePageParamsAndGetValid(pageParams.getPageNumber(), pageParams.getPageSize());

        return paymentRepository.findAllByFilter(
                paymentFilter.getFrom(),
                paymentFilter.getTo(),
                paymentFilter.getId(),
                paymentFilter.getInquiryRefId(),
                paymentFilter.getStatus(),
                pageable).getContent();
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findById(UUID id) {
        return paymentRepository.findById(id).orElse(null);
    }
}
