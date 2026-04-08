package com.lms.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.lms.quiz",
        "com.lms.content.common",
        "com.lms.common"
})
@EntityScan(basePackages = {
        "com.lms.quiz.entity",
        "com.lms.content.common.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.lms.quiz.repository"
})
public class RepoQuizApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoQuizApplication.class, args);
    }
}
