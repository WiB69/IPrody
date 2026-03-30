package com.iprody.crm.inquiryservice;

import org.springframework.boot.SpringApplication;

public class TestInquiryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(InquiryServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
