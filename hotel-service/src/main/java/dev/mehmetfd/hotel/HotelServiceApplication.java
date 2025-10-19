package dev.mehmetfd.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import dev.mehmetfd.common.context.CustomRequestContextFilter;
import dev.mehmetfd.common.exception.GlobalExceptionHandler;

@SpringBootApplication
@Import(GlobalExceptionHandler.class)
@EnableDiscoveryClient
public class HotelServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelServiceApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<CustomRequestContextFilter> customRequestContextFilter() {
        FilterRegistrationBean<CustomRequestContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CustomRequestContextFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
