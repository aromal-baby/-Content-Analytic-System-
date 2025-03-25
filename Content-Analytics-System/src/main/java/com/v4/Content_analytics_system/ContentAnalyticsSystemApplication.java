package com.v4.Content_analytics_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling	// For the mongo data storing purpose
public class ContentAnalyticsSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContentAnalyticsSystemApplication.class, args);
	}

}
