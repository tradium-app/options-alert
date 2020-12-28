package net.surajshrestha.optionsalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OptionsAlertApplication {

	public static void main(String[] args) {
		SpringApplication.run(OptionsAlertApplication.class, args);
	}

}
