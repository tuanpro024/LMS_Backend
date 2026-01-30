package com.lms.learningpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.lms.learningpath",
		"com.lms.common",
		"com.lms.content.common"
})
@EntityScan(basePackages = {
		"com.lms.learningpath.entity",
		"com.lms.content.common.entity"
})
@EnableJpaRepositories(basePackages = {
		"com.lms.learningpath.repository",
		"com.lms.content.common.repository"
})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class RepoLearningPathApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepoLearningPathApplication.class, args);
	}

}
