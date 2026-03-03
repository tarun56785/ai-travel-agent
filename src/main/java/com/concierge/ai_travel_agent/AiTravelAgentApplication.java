package com.concierge.ai_travel_agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AiTravelAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiTravelAgentApplication.class, args);
	}

}
