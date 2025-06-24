package com.example.filedownloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FiledownloaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(FiledownloaderApplication.class, args);
	}

}
