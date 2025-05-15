package com.example.supervision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.supervision.client")
public class SupervisionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupervisionApplication.class, args);
	}

}
