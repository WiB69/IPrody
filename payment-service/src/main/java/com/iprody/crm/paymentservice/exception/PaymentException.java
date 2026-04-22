package com.iprody.crm.paymentservice.exception;

public class PaymentException extends RuntimeException{

   private String message;

    public PaymentException(String message) {
        super(message);
    }
}
