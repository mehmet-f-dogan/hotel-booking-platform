package dev.mehmetfd.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NotificationServiceApplication.class);

        app.setBanner((environment, sourceClass, out) -> {
            out.println("=== Notification Service ===");
        });
    }
}
