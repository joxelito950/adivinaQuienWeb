package com.joxelito.adivinaquien;

import com.joxelito.adivinaquien.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AdivinaquienApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdivinaquienApplication.class, args);
	}

}
