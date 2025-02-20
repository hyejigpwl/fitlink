package com.lec.packages.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.lec.packages.converter.StringToExerciseCodeConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToExerciseCodeConverter stringToExerciseCodeConverter;
    

    public WebConfig(StringToExerciseCodeConverter stringToExerciseCodeConverter) {
    	
        this.stringToExerciseCodeConverter = stringToExerciseCodeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToExerciseCodeConverter);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

