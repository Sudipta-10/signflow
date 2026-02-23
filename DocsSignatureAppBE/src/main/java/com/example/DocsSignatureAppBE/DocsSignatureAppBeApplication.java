package com.example.DocsSignatureAppBE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DocsSignatureAppBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocsSignatureAppBeApplication.class, args);
	}

    @GetMapping("/")
    public String home() {
        return "Verifile API is running successfully!";
    }

}
