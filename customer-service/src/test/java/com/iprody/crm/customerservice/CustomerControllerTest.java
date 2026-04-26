package com.iprody.crm.customerservice;

import com.iprody.crm.customerservice.controller.CustomerController;
import com.iprody.crm.customerservice.dto.CustomerDataDto;
import com.iprody.crm.customerservice.entity.Contract;
import com.iprody.crm.customerservice.entity.Customer;
import com.iprody.crm.customerservice.service.CustomerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @MockitoBean
    private CustomerServiceImpl customerService;

    private Customer createTestCustomer(UUID id) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Contract contract = new Contract();
        contract.setId(UUID.randomUUID());
        contract.setEmail("john@example.com");
        contract.setPhoneNumber("+1234567890");
        contract.setCreatedAt(now);
        contract.setUpdatedAt(now);

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFullName("John Doe");
        customer.setContract(contract);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        return customer;
    }

    @Nested
    class SaveTests {

        @Test
        void shouldReturnBadRequestWhenSaveWithBlankFullName() throws Exception {
            CustomerDataDto invalidDto = new CustomerDataDto("");
            invalidDto.setContract(null);

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(customerService);
        }

        @Test
        void shouldReturnBadRequestWhenEmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON.toString())
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(customerService);
        }

        @Test
        void shouldSaveCustomer() throws Exception {
            CustomerDataDto inputDto = new CustomerDataDto("New Customer");

            Customer savedCustomer = new Customer();
            savedCustomer.setId(UUID.randomUUID());
            savedCustomer.setFullName("New Customer");

            when(customerService.save(any())).thenReturn(savedCustomer);
            mockMvc.perform(post("/api/v1/customers")
                            .contentType(org.junit.jupiter.api.MediaType.APPLICATION_JSON.toString())
                            .content(OBJECT_MAPPER.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("New Customer"))
                    .andExpect(jsonPath("$.id").exists());

            verify(customerService).save(any());
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldReturnBadRequestWhenUpdateWithBlankFullName() throws Exception {
            UUID customerId = UUID.randomUUID();
            CustomerDataDto requestDto = new CustomerDataDto("");

            mockMvc.perform(put("/api/v1/customers/{id}", customerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(OBJECT_MAPPER.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(customerService);
        }

        @Test
        void shouldUpdateCustomer() throws Exception {
            UUID id = UUID.randomUUID();
            CustomerDataDto updateDto = new CustomerDataDto("Updated Name");

            Customer updatedCustomer = new Customer();
            updatedCustomer.setId(id);
            updatedCustomer.setFullName("Updated Name");

            when(customerService.update(eq(id), any())).thenReturn(updatedCustomer);

            mockMvc.perform(put("/api/v1/customers/{id}", id)
                            .contentType(org.junit.jupiter.api.MediaType.APPLICATION_JSON.toString())
                            .content(OBJECT_MAPPER.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Updated Name"));

            verify(customerService).update(eq(id), any());
        }
    }

    @Nested
    class SearchTests {

        @Test
        void shouldReturnBadRequestWhenOffsetNegative() throws Exception {
            mockMvc.perform(get("/api/v1/customers/search")
                            .param("offset", "-1")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(customerService);
        }

        @Test
        void shouldReturnBadRequestWhenLimitNegative() throws Exception {
            mockMvc.perform(get("/api/v1/customers/search")
                            .param("offset", "0")
                            .param("limit", "-1"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(customerService);
        }

        @Test
        void shouldReturnBadRequestWhenLimitExceedsMax() throws Exception {
            mockMvc.perform(get("/api/v1/customers/search")
                            .param("offset", "0")
                            .param("limit", "1001"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(customerService);
        }

        @Test
        void shouldReturnEmptyListWhenNoCustomersFound() throws Exception {
            when(customerService.findAllByFilter(any(), any(), any(), any()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/customers/search")
                            .param("offset", "0")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(customerService).findAllByFilter(any(), any(), any(), any());
        }

        @Test
        void shouldFindAllByFilter() throws Exception {
            Customer customer1 = createTestCustomer(UUID.randomUUID());
            Customer customer2 = createTestCustomer(UUID.randomUUID());
            customer2.setFullName("Another Customer");

            when(customerService.findAllByFilter(any(), any(), any(), any()))
                    .thenReturn(List.of(customer1, customer2));

            mockMvc.perform(get("/api/v1/customers/search")
                            .param("offset", "0")
                            .param("limit", "10")
                            .param("filter.fullName", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                    .andExpect(jsonPath("$[1].fullName").value("Another Customer"));

            verify(customerService).findAllByFilter(any(), any(), any(), any());
        }

        @Test
        void shouldFindAllByFilterWithSorting() throws Exception {
            when(customerService.findAllByFilter(any(), any(), any(), any()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/customers/search")
                            .param("offset", "0")
                            .param("limit", "10")
                            .param("filter.fullName", "John")
                            .param("sortDto.sortField", "FULL_NAME")
                            .param("sortDto.sortDirection", "ASC"))
                    .andExpect(status().isOk());

            verify(customerService).findAllByFilter(any(), any(), any(), any());
        }
    }
}