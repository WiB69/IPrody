package com.iprody.crm.inquiryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iprody.crm.inquiryservice.controller.InquiryController;
import com.iprody.crm.inquiryservice.model.dto.InquiryDataDto;
import com.iprody.crm.inquiryservice.model.dto.InquiryDto;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import com.iprody.crm.inquiryservice.service.InquiryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.iprody.crm.inquiryservice.enums.InquiryStatus.NEW;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryController.class)
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @MockitoBean
    private InquiryServiceImpl inquiryService;

    @Test
    void shouldSaveInquiry() throws Exception {
        InquiryDataDto requestDto = new InquiryDataDto();
        requestDto.setProductRefId(UUID.randomUUID());
        requestDto.setCustomerRefId(UUID.randomUUID());
        requestDto.setManagerRefId(UUID.randomUUID());
        requestDto.setSource("WEB");

        Inquiry inquiry = new Inquiry();
        inquiry.setId(UUID.randomUUID());
        inquiry.setStatus(NEW);

        when(inquiryService.save(any())).thenReturn(inquiry);

        mockMvc.perform(post("/api/v1/inquires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void shouldFindAllByFilter() throws Exception {
        InquiryDto inquiryDto = new InquiryDto();
        inquiryDto.setId(UUID.randomUUID());
        inquiryDto.setStatus(NEW);

        when(inquiryService.findAllByFilter(any(), any(), any(), any()))
                .thenReturn(List.of(new Inquiry()));

        mockMvc.perform(get("/api/v1/inquires/search")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnBadRequestWhenSaveWithInvalidData() throws Exception {
        InquiryDataDto requestDto = new InquiryDataDto();

        mockMvc.perform(post("/api/v1/inquires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}
