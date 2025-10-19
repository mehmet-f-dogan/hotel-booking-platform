package dev.mehmetfd.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApiGatewayApplication.class);

        app.setBanner((environment, sourceClass, out) -> {
            out.println("=== API Gateway ===");
        });
    }
}