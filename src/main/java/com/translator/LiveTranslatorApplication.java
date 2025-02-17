package com.translator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiveTranslatorApplication {

	public static void main(String[] args) {
				SpringApplication.run(LiveTranslatorApplication.class, args);
		System.out.println("I am running the application");
	}

}
