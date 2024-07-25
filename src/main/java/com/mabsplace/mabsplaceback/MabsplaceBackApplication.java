package com.mabsplace.mabsplaceback;

import com.mabsplace.mabsplaceback.security.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
@EnableScheduling
public class MabsplaceBackApplication {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.ionos.fr");
		mailSender.setPort(465); // You can also use port 465 for SSL

		mailSender.setUsername("karl.mabou@mabsplace.com"); // Replace with your Ionos email
		mailSender.setPassword("Karlmabs1!"); // Replace with your Ionos email password

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true"); // For port 587
		props.put("mail.smtp.ssl.enable", "true"); // If you use port 465

		return mailSender;
	}

	public static void main(String[] args) {
		SpringApplication.run(MabsplaceBackApplication.class, args);
	}

}
