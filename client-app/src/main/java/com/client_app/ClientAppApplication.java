package com.client_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.client_app", "com.uber.common"})
@EnableDiscoveryClient
public class ClientAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(ClientAppApplication.class, args);
	}
}
