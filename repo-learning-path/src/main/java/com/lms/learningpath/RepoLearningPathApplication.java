package com.lms.learningpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
		"com.lms.learningpath",
		"com.lms.common"
})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class RepoLearningPathApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepoLearningPathApplication.class, args);
	}

}
