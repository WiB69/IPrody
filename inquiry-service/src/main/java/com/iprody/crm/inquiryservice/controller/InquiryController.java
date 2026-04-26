package com.iprody.crm.inquiryservice.controller;

import com.iprody.crm.inquiryservice.mapper.InquiryMapper;
import com.iprody.crm.inquiryservice.model.dto.InquiryDataDto;
import com.iprody.crm.inquiryservice.model.dto.InquiryDto;
import com.iprody.crm.inquiryservice.model.dto.InquiryRecordRequestDto;
import com.iprody.crm.inquiryservice.service.InquiryServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/inquires", produces = MediaType.APPLICATION_JSON_VALUE)
public class InquiryController {
    private final InquiryServiceImpl inquiryService;

    @PostMapping
    public InquiryDto save(@Valid @RequestBody InquiryDataDto dto) {
        log.info("Received request to save inquiry: {}", dto);
        return InquiryMapper.INSTANCE.toDto(
                inquiryService.save(InquiryMapper.INSTANCE.toData(dto)
                )
        );
    }

    @GetMapping("/search")
    public List<InquiryDto> findAllByFilter(@Valid InquiryRecordRequestDto inquiryRecordRequestDto) {
        log.info("Received request to find all inquiries by filter: {}", inquiryRecordRequestDto);
        return InquiryMapper.INSTANCE.toDtoList(
                inquiryService.findAllByFilter(
                        InquiryMapper.INSTANCE.toFilter(inquiryRecordRequestDto.getFilter()),
                        inquiryRecordRequestDto.getOffset(),
                        inquiryRecordRequestDto.getLimit(),
                        InquiryMapper.INSTANCE.toSorting(inquiryRecordRequestDto.getSortDto())
                )
        );
    }
}