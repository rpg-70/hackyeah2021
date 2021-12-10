package com.bikpicture.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BikPictureApplication {

	public static void main(String[] args) {
		SpringApplication.run(BikPictureApplication.class, args);
	}

}
