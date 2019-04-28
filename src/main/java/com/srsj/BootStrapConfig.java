package com.srsj;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.InitParameterConfiguringServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BootStrapConfig {
//    @Value("${logbackConfigLocation}")
//    private String logbackConfigLocation;

//    @Bean
//    public InitParameterConfiguringServletContextInitializer initParamsInitializer() {
//        Map<String, String> contextParams = new HashMap<>();
//        contextParams.put("logbackExposeWebAppRoot", "false");
//        contextParams.put("logbackConfigLocation", logbackConfigLocation);
//        return new InitParameterConfiguringServletContextInitializer(contextParams);
//    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        return threadPoolTaskExecutor;
    }
}
