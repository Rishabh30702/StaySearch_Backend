package com.example.StaySearch.StaySearchBackend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@SpringBootApplication

@EnableScheduling
@EnableTransactionManagement
public class StaySearchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaySearchBackendApplication.class, args);
	}
	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		System.out.println("Spring Boot Application TimeZone set to Asia/Kolkata");
	}
}
