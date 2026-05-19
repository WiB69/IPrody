package com.iprody.crm.paymentservice.client;

import com.iprody.crm.paymentservice.config.XPaymentClientConfig;
import com.iprody.crm.paymentservice.model.dto.XPaymentRequest;
import com.iprody.crm.paymentservice.model.dto.XPaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class XPaymentClient {

    private final RestTemplate restTemplate;
    private final XPaymentClientConfig config;

    @Autowired
    public XPaymentClient(RestTemplate restTemplate, XPaymentClientConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public XPaymentResponse createPayment(XPaymentRequest request) {
        String url = config.getApiUrl() + "/charges";
        HttpHeaders headers = config.createHttpHeaders();
        HttpEntity<XPaymentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<XPaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                XPaymentResponse.class
        );

        return response.getBody();
    }

    public XPaymentResponse getPaymentStatus(String paymentId) {
        String url = config.getApiUrl() + "/charges/" + paymentId;
        HttpHeaders headers = config.createHttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<XPaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                XPaymentResponse.class
        );

        return response.getBody();
    }
}