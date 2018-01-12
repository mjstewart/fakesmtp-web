package app.configuration.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Need to handle config for all non spring data rest controllers
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*");
    }
}
