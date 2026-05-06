package com.iprody.crm.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class XPaymentClientConfig {

    @Value("${xpayment.api.url}")
    private String apiUrl;

    @Value("${xpayment.api.username:paymentAgentIprody}")
    private String username;

    @Value("${xpayment.api.password:iprodyTestPassword0123}")
    private String password;

    @Value("${xpayment.api.account-header:paymentAgentIprodyApiToken}")
    private String xPayAccountHeader;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.add("Authorization", "Basic " + encodedAuth);

        headers.add("X-Pay-Account", xPayAccountHeader);

        headers.add("Content-Type", "application/json");

        return headers;
    }
}