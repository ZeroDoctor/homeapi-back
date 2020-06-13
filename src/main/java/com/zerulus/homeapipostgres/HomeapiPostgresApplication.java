package com.zerulus.homeapipostgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class HomeapiPostgresApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeapiPostgresApplication.class, args);
	}
}
