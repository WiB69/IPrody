package com.iprody.crm.inquiryservice.mapper;

import com.iprody.crm.inquiryservice.enums.InquirySortField;
import com.iprody.crm.inquiryservice.model.dto.*;
import com.iprody.crm.inquiryservice.model.entity.Inquiry;
import com.iprody.crm.inquiryservice.utils.Sorting;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Sort;

import java.util.List;

@Mapper
public interface InquiryMapper {
    InquiryMapper INSTANCE = Mappers.getMapper(InquiryMapper.class);

    Inquiry fromData(InquiryData data);

    @Mapping(target = "status", source = "data.status")
    @Mapping(target = "managerRefId", source = "data.managerRefId")
    Inquiry update(Inquiry inquiry, InquiryUpdateData data);

    InquiryDto toDto(Inquiry inquiry);

    InquiryData toData(InquiryDataDto dto);

    List<InquiryDto> toDtoList(List<Inquiry> customerList);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    InquiryFilter toFilter(InquiryFilterDto dto);

    default Sorting toSorting(SortingDto sortingDto) {
        if (sortingDto == null) {
            return new Sorting(InquirySortField.CREATED_AT, Sort.Direction.DESC);
        }

        InquirySortField sortField = InquirySortField.CREATED_AT;
        if (sortingDto.getSortField() != null) {
            try {
                sortField = InquirySortField.valueOf(sortingDto.getSortField().name());
            } catch (IllegalArgumentException ignored) {}
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (sortingDto.getSortDirection() != null) {
            direction = switch (sortingDto.getSortDirection()) {
                case ASC -> Sort.Direction.ASC;
                case DESC -> Sort.Direction.DESC;
            };
        }

        return new Sorting(sortField, direction);
    }
}
