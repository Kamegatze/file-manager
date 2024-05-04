package com.kamegatze.file.manager;

import com.kamegatze.file.manager.configuration.security.authentication.jwt.converter.JwtRemoteConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileManagerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(FileManagerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	}
}
