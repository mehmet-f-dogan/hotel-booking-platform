package dev.mehmetfd.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AuthServiceApplication.class);

        app.setBanner((environment, sourceClass, out) -> {
            out.println("=== Auth Service ===");
        });
    }
}
