package com.iprody.crm.inquiryservice.service;

import com.iprody.crm.inquiryservice.enums.InquiryStatus;
import com.iprody.crm.inquiryservice.exception.ResourceNotFoundException;
import com.iprody.crm.inquiryservice.mapper.InquiryMapper;
import com.iprody.crm.inquiryservice.model.dto.InquiryData;
import com.iprody.crm.inquiryservice.model.dto.InquiryFilter;
import com.iprody.crm.inquiryservice.model.dto.InquiryUpdateData;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import com.iprody.crm.inquiryservice.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iprody.crm.inquiryservice.utils.Sorting;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public Inquiry save(InquiryData data) {
        log.debug("Saving new inquiry with data: {}", data);
        Inquiry inquiry = InquiryMapper.INSTANCE.fromData(data);
        inquiry.setStatus(InquiryStatus.NEW);
        Inquiry saved = inquiryRepository.save(inquiry);
        log.info("Saved inquiry with id: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Inquiry findById(UUID id) {
        log.debug("Finding inquiry by id: {}", id);
        return inquiryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Inquiry not found with id: {}", id);
                    return new ResourceNotFoundException("Inquiry not found with id: " + id);
                });
    }

    @Transactional
    public Inquiry update(UUID id, InquiryUpdateData data) {
        log.debug("Updating inquiry with id: {}", id);
        Inquiry existingInquiry = findById(id);
        Inquiry updated = InquiryMapper.INSTANCE.update(existingInquiry, data);
        Inquiry saved = inquiryRepository.save(updated);
        log.info("Updated inquiry with id: {}", id);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findAllByFilter(InquiryFilter filter, Integer offset, Integer limit, Sorting sorting) {
        log.debug("Finding all inquiries by filter: {}, offset: {}, limit: {}, sorting: {}", filter, offset, limit, sorting);

        if (offset == null) offset = 0;
        if (limit == null) limit = 20;

        PageRequest pageRequest = PageRequest.of(
                offset / limit,
                limit,
                Sort.by(sorting.getSortDirection(), sorting.getSortField().getFieldName())
        );

        return inquiryRepository.findAllByFilter(
                        filter.getStatus(),
                        filter.getCustomerRefId(),
                        filter.getManagerRefId(),
                        pageRequest)
                .getContent();
    }
}