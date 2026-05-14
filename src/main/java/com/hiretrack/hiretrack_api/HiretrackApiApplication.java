package com.hiretrack.hiretrack_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HiretrackApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HiretrackApiApplication.class, args);
	}

}
