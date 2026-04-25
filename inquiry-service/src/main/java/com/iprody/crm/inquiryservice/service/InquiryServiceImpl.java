package com.iprody.crm.inquiryservice.service;

import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import com.iprody.crm.inquiryservice.mapper.InquiryMapper;
import com.iprody.crm.inquiryservice.model.dto.InquiryData;
import com.iprody.crm.inquiryservice.model.dto.InquiryFilter;
import com.iprody.crm.inquiryservice.model.dto.InquiryUpdateData;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import com.iprody.crm.inquiryservice.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iprody.crm.inquiryservice.utils.Sorting;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public Inquiry save(InquiryData data) {
        Inquiry inquiry = InquiryMapper.INSTANCE.fromData(data);
        inquiry.setStatus(InquiryStatus.NEW);
        return inquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public Inquiry findById(UUID id) {
        return inquiryRepository.findById(id).orElse(null);
    }

    @Transactional
    public Inquiry update(UUID id, InquiryUpdateData data) {
        return inquiryRepository.save(
                InquiryMapper.INSTANCE.update(
                        findById(id),
                        data
                )
        );
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findAllByFilter(InquiryFilter filter, Integer offset, Integer limit, Sorting sorting) {
        return inquiryRepository.findAllByFilter(
                filter.getStatus(),
                filter.getCustomerRefId(),
                filter.getManagerRefId(),
                PageRequest.of(
                        offset,
                        limit,
                        Sort.by(sorting.getSortDirection(), sorting.getSortField().getFieldName())))
                .getContent();
    }
}
