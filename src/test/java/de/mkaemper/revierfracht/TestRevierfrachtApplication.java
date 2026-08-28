package de.mkaemper.revierfracht;

import org.springframework.boot.SpringApplication;

public class TestRevierfrachtApplication {

	public static void main(String[] args) {
		SpringApplication.from(RevierfrachtApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
