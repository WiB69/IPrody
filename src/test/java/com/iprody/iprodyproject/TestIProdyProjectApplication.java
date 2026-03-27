package com.iprody.iprodyproject;

import org.springframework.boot.SpringApplication;

public class TestIProdyProjectApplication {

	public static void main(String[] args) {
		SpringApplication.from(IProdyProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
