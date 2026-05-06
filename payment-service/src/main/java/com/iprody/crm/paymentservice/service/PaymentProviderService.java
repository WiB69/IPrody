package com.iprody.crm.paymentservice.service;

import com.iprody.crm.paymentservice.client.XPaymentClient;
import com.iprody.crm.paymentservice.exception.PaymentException;
import com.iprody.crm.paymentservice.model.dto.XPaymentRequest;
import com.iprody.crm.paymentservice.model.dto.XPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProviderService {

    private final XPaymentClient xPaymentClient;

    public XPaymentResponse initiatePayment(XPaymentRequest request) {
        log.info("Initiating payment for order: {}", request.getOrder());

        try {
            XPaymentResponse response = xPaymentClient.createPayment(request);
            log.info("Payment initiated successfully with id: {}, status: {}",
                    response.getId(), response.getStatus());

            return response;

        } catch (Exception e) {
            throw new PaymentException("Failed to initiate payment: " + e.getMessage(), e);
        }
    }

    public XPaymentResponse checkPaymentStatus(UUID transactionId) {
        log.info("Checking payment status for transactionId: {}", transactionId);

        try {
            XPaymentResponse response = xPaymentClient.getPaymentStatus(transactionId.toString());
            log.info("Payment status for transactionId {}: {}", transactionId, response.getStatus());

            return response;

        } catch (Exception e) {
            throw new PaymentException("Failed to check payment status: " + e.getMessage(), e);
        }
    }
}