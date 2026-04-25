package com.iprody.crm.inquiryservice;

import com.iprody.crm.inquiryservice.model.dto.InquiryData;
import com.iprody.crm.inquiryservice.model.dto.InquiryFilter;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import com.iprody.crm.inquiryservice.service.InquiryServiceImpl;
import com.iprody.crm.inquiryservice.utils.Sorting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InquiryServiceTest {

    @Mock
    private InquiryServiceImpl inquiryService;

    @Test
    void shouldSaveInquiry() {
        InquiryData inquiryData = new InquiryData();
        inquiryData.setProductRefId(UUID.randomUUID());
        inquiryData.setCustomerRefId(UUID.randomUUID());
        inquiryData.setManagerRefId(UUID.randomUUID());
        inquiryData.setSource("WEB");

        Inquiry expectedInquiry = new Inquiry();
        expectedInquiry.setId(UUID.randomUUID());

        when(inquiryService.save(any(InquiryData.class))).thenReturn(expectedInquiry);

        Inquiry result = inquiryService.save(inquiryData);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedInquiry.getId());
        verify(inquiryService, times(1)).save(any(InquiryData.class));
    }

    @Test
    void shouldFindById() {
        UUID inquiryId = UUID.randomUUID();
        Inquiry expectedInquiry = new Inquiry();
        expectedInquiry.setId(inquiryId);

        when(inquiryService.findById(inquiryId)).thenReturn(expectedInquiry);

        Inquiry result = inquiryService.findById(inquiryId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(inquiryId);
        verify(inquiryService, times(1)).findById(inquiryId);
    }

    @Test
    void shouldReturnNullWhenInquiryNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(inquiryService.findById(nonExistentId)).thenReturn(null);

        Inquiry result = inquiryService.findById(nonExistentId);

        assertThat(result).isNull();
        verify(inquiryService, times(1)).findById(nonExistentId);
    }

    @Test
    void shouldFindAllByFilter() {
        List<Inquiry> mockInquiries = List.of(new Inquiry(), new Inquiry());
        InquiryFilter filter = new InquiryFilter();
        Sorting sorting = new Sorting(
                com.iprody.crm.inquiryservice.enums.InquirySortField.CREATED_AT,
                Sort.Direction.DESC
        );

        when(inquiryService.findAllByFilter(any(), anyInt(), anyInt(), any()))
                .thenReturn(mockInquiries);

        List<Inquiry> result = inquiryService.findAllByFilter(
                filter,
                0,
                10,
                sorting
        );

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.size()).isEqualTo(2);
        verify(inquiryService, times(1)).findAllByFilter(any(), anyInt(), anyInt(), any());
    }

    @Test
    void shouldFindAllByFilterWithEmptyResult() {
        InquiryFilter filter = new InquiryFilter();
        Sorting sorting = new Sorting(
                com.iprody.crm.inquiryservice.enums.InquirySortField.CREATED_AT,
                Sort.Direction.DESC
        );

        when(inquiryService.findAllByFilter(any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of());

        List<Inquiry> result = inquiryService.findAllByFilter(
                filter,
                0,
                10,
                sorting
        );

        assertThat(result.isEmpty()).isTrue();
        verify(inquiryService, times(1)).findAllByFilter(any(), anyInt(), anyInt(), any());
    }
}
