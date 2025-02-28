package com.example.biofit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class BiofitApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiofitApplication.class, args);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/login")
	public String test() {
		return "CORS test successful!";
	}

}
