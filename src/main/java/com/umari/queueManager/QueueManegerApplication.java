package com.umari.queueManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QueueManegerApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueueManegerApplication.class, args);
	}

}
