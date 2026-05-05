package com.iprody.crm.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class is used to create and update data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDataDto {
    @NotBlank
    @Size(max = 30, message = "Name is too long (max 30 symbols)")
    private String fullName;
    private ContractDataDto contract;

    public CustomerDataDto(String fullName) {
        this.fullName = fullName;
    }
}
