package com.example.sftpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SftpclientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SftpclientApplication.class, args);
	}

}
