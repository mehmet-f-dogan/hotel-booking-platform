package dev.mehmetfd.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import dev.mehmetfd.common.context.RequestContextFilter;

@SpringBootApplication
@EnableDiscoveryClient
public class ReservationServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ReservationServiceApplication.class);

        app.setBanner((environment, sourceClass, out) -> {
            out.println("=== Reservation Service ===");
        });
    }

    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
        FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestContextFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
