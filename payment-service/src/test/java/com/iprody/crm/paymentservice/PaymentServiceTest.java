package com.iprody.crm.paymentservice;

import com.iprody.crm.paymentservice.model.dto.PaymentFilter;
import com.iprody.crm.paymentservice.model.entity.Payment;
import com.iprody.crm.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentService paymentService;

    @Test
    void shouldFindAllByFilter() {
        Page<Payment> mockPage = new PageImpl<>(List.of(new Payment()), PageRequest.of(0, 10), 1);
        when(paymentService.findAllByFilter(any(), any())).thenReturn(mockPage.getContent());

        List<Payment> result = paymentService.findAllByFilter(
                new PaymentFilter(),
                PageRequest.of(0, 10, Sort.Direction.ASC, "id"));

        assertThat(!result.isEmpty());
        verify(paymentService, times(1)).findAllByFilter(any(), any());
    }

    @Test
    void shouldFindAllByFilterWithEmptyResult() {
        when(paymentService.findAllByFilter(any(), any())).thenReturn(List.of());

        List<Payment> result = paymentService.findAllByFilter(
                new PaymentFilter(),
                PageRequest.of(0, 10, Sort.Direction.ASC, "id"));

        assertThat(result.isEmpty()).isTrue();
        verify(paymentService, times(1)).findAllByFilter(any(), any());
    }
}
