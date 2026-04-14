package com.lms.videocourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@ComponentScan(basePackages = {
		"com.lms.videocourse",
		"com.lms.common",
		"com.lms.content.common"
})
@EntityScan(basePackages = {
		"com.lms.videocourse.entity",
		"com.lms.common.jpa",
		"com.lms.content.common.entity"
})
@EnableJpaRepositories(basePackages = {
		"com.lms.videocourse.repository"

})
public class RepoVideoCourseApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepoVideoCourseApplication.class, args);
	}

}
