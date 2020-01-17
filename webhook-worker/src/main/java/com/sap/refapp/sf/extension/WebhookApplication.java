package com.sap.refapp.sf.extension;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WebhookApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(WebhookApplication.class, args);
	}
	
	 /**
     * @param builder
     * @return RestTemplate
     */
    @Bean
    public RestTemplate rest(RestTemplateBuilder builder) {
      return builder.build();
    }
}
