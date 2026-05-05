package com.iprody.crm.inquiryservice.service;

import com.iprody.crm.inquiryservice.model.dto.InquiryData;
import com.iprody.crm.inquiryservice.model.dto.InquiryFilter;
import com.iprody.crm.inquiryservice.model.dto.InquiryUpdateData;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import com.iprody.crm.inquiryservice.utils.Sorting;

import java.util.List;
import java.util.UUID;

public interface InquiryService {

    Inquiry save(InquiryData data);

    Inquiry findById(UUID id);

    Inquiry update(UUID id, InquiryUpdateData data);

    List<Inquiry> findAllByFilter(InquiryFilter filter, Integer offset, Integer limit, Sorting sorting);
}
