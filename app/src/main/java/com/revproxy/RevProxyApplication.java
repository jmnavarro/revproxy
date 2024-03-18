package com.revproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class RevProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevProxyApplication.class, args);
	}

}
