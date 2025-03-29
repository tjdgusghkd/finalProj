package com.hapla.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hapla.common.interceptor.AccessLogInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{

	 @Autowired
	    private AccessLogInterceptor accessLogInterceptor;
	    
	 @Override
	    public void addInterceptors(InterceptorRegistry registry) {
	        registry.addInterceptor(accessLogInterceptor)
	                .addPathPatterns("/**")
	                .excludePathPatterns(
	                    "/resources/**", 
	                    "/css/**", 
	                    "/js/**", 
	                    "/images/**", 
	                    "/error/**", 
	                    "/admin/accessStats/**",
	                    "/admin/accessStats",
	                    "/admin/createTestData",
	                    "/admin/createTestData/**",
	                    "/admin-css/**",
	                    "/favicon.ico",
	                    "/static/**",
	                    "/admin/api/**"
	                );
	    }
	 
	 @Bean
	    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
	        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
	                .failOnEmptyBeans(false) // FAIL_ON_EMPTY_BEANS 비활성화
	                .build();
	        
	        // 직렬화 관련 추가 설정
	        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	        
	        return new MappingJackson2HttpMessageConverter(mapper);
	    }
	
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/profiles/**") // /profiles/로 시작하는 요청만 매핑
				.addResourceLocations("file:///c:/profiles/");
		registry.addResourceHandler("/uploadFiles/**") // /uploadFiles/로 시작하는 요청만 매핑
				.addResourceLocations("file:///c:/uploadFiles/");
		registry.addResourceHandler("/static/**") // /static/으로 시작하는 요청만 매핑
				.addResourceLocations("classpath:/static/");
	}
}
