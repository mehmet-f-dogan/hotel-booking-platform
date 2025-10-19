package dev.mehmetfd.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import dev.mehmetfd.common.context.CustomRequestContextFilter;

@SpringBootApplication
@EnableDiscoveryClient
public class ReservationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<CustomRequestContextFilter> customRequestContextFilter() {
        FilterRegistrationBean<CustomRequestContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CustomRequestContextFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
