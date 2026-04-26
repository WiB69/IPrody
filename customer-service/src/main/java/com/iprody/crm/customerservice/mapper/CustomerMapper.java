package com.iprody.crm.customerservice.mapper;

import com.iprody.crm.customerservice.dto.*;
import com.iprody.crm.customerservice.entity.Customer;
import com.iprody.crm.customerservice.enums.CustomerSortField;
import com.iprody.crm.customerservice.utils.Sorting;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Sort;

import java.util.List;

@Mapper
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerDto toDto(Customer customer);

    @Mapping(target = "contract.email", source = "contract.email")
    @Mapping(target = "contract.phoneNumber", source = "contract.phoneNumber")
    CustomerData toData(CustomerDataDto dto);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    CustomerFilter toFilter(CustomerFilterDto dto);

    List<CustomerDto> toDtoList(List<Customer> customerResultList);

    default Sorting toSorting(SortingDto sortingDto) {
        if (sortingDto == null) {
            return new Sorting(CustomerSortField.FULL_NAME, Sort.Direction.DESC);
        }

        CustomerSortField sortField = CustomerSortField.FULL_NAME;
        if (sortingDto.getSortField() != null) {
            try {
                sortField = CustomerSortField.valueOf(sortingDto.getSortField().name());
            } catch (IllegalArgumentException ignored) {
                throw new IllegalArgumentException("Unexpected enum constant");
            }
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
