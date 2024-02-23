package eu.macarropueyo.terapweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("eu.macarropueyo.terapweb")
public class TerapwebApplication {

	public static void main(String[] args) {
		SpringApplication.run(TerapwebApplication.class, args);
	}

}
