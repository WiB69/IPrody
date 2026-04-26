package com.iprody.crm.customerservice.controller;

import com.iprody.crm.customerservice.dto.CustomerDataDto;
import com.iprody.crm.customerservice.dto.CustomerDto;
import com.iprody.crm.customerservice.dto.CustomerRecordRequestDto;
import com.iprody.crm.customerservice.mapper.CustomerMapper;
import com.iprody.crm.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(value = "/api/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/{id}")
    public CustomerDto getById(@PathVariable("id") UUID id) {
        return CustomerMapper.INSTANCE.toDto(customerService.findById(id));
    }

    @PostMapping
    public CustomerDto save(@Valid @RequestBody CustomerDataDto dto) {
        return CustomerMapper.INSTANCE.toDto(
                customerService.save(
                        CustomerMapper.INSTANCE.toData(dto)
                )
        );
    }

    @PutMapping("/{id}")
    public CustomerDto update(@PathVariable("id") UUID id,
                              @Valid @RequestBody CustomerDataDto dto) {
        return CustomerMapper.INSTANCE.toDto(
                customerService.update(
                        id,
                        CustomerMapper.INSTANCE.toData(dto)
                )
        );
    }

    @GetMapping("/search")
    public List<CustomerDto> findAllByFilter(@Valid CustomerRecordRequestDto customerRecordRequestDto) {
        return CustomerMapper.INSTANCE.toDtoList(
                customerService.findAllByFilter(
                        CustomerMapper.INSTANCE.toFilter(customerRecordRequestDto.getFilter()),
                        customerRecordRequestDto.getOffset(),
                        customerRecordRequestDto.getLimit(),
                        CustomerMapper.INSTANCE.toSorting(customerRecordRequestDto.getSortDto())));
    }
}
