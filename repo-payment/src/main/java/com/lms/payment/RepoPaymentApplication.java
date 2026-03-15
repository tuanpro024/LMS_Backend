package com.lms.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {
		"com.lms.payment",
		"com.lms.common"
})
@EntityScan(basePackages = {
		"com.lms.payment.entity",
		"com.lms.common.jpa"
})
@EnableJpaRepositories(basePackages = {
		"com.lms.payment.repository"
})
public class RepoPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepoPaymentApplication.class, args);
	}

}
