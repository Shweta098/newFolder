package com.appsdeveloperblog.photoapp.api.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.appsdeveloperblog.photoapp.api.users.shared.FeignErrorDecoder;

import feign.Logger;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PhotoAppApiUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoAppApiUsersApplication.class, args);
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
	
	//Implements httpexchange
	@Bean
	//HttpExchangeRepository : records and store http exchanges that occur in spring boot application
	public HttpExchangeRepository httpTraceRepository() {
		return new InMemoryHttpExchangeRepository(); //default impl of HttpExchangeRepository that stores http exchanges in memory
		//we can create our own http exchange repo and use it to store http exchanges in different locations(eg in db or file)
	}
	
	@Bean
	@LoadBalanced // To enable client side load balance
	public RestTemplate getRestTemplate(RestTemplateBuilder builder) {
//		return new RestTemplate();
		return builder.build();
	}
	
	//To enable logging for feign
	@Bean
	Logger.Level feignLoggerLover() {
		return Logger.Level.FULL;
	}
	
	/*
	 * @Bean public FeignErrorDecoder getFeignErrorDecoder() { return new
	 * FeignErrorDecoder(); }
	 */

}
