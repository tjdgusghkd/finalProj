package com.hapla;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class HaplaApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load(); // .env 파일 로드
		System.setProperty("kakao_client_id", dotenv.get("kakao_client_id"));
		System.setProperty("google_api_key", dotenv.get("google_api_key"));
		System.setProperty("google_client_id", dotenv.get("google_client_id"));
		System.setProperty("google_client_secret", dotenv.get("google_client_secret"));
		System.setProperty("AMADEUS_API_ID", dotenv.get("AMADEUS_API_ID"));
		System.setProperty("AMADEUS_API_KEY", dotenv.get("AMADEUS_API_KEY"));
		System.setProperty("r2.access-key", dotenv.get("r2.access-key"));
		System.setProperty("r2.secret-key", dotenv.get("r2.secret-key"));

		SpringApplication.run(HaplaApplication.class, args);
		
	}

}
