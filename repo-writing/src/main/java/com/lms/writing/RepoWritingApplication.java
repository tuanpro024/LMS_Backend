package com.lms.writing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
                "com.lms.writing",
                "com.lms.content.common",
                "com.lms.common"

})
@EntityScan(basePackages = {
                "com.lms.writing.entity",
                "com.lms.content.common.entity"
})
@EnableJpaRepositories(basePackages = {
                "com.lms.writing.repository"
})
public class RepoWritingApplication {

        public static void main(String[] args) {
                SpringApplication.run(RepoWritingApplication.class, args);
        }
}
