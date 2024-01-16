package com.mabsplace.mabsplaceback;

import com.mabsplace.mabsplaceback.security.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MabsplaceBackApplication {


	public static void main(String[] args) {
		SpringApplication.run(MabsplaceBackApplication.class, args);
	}

}
