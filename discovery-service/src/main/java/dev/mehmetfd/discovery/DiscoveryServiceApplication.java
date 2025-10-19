package dev.mehmetfd.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DiscoveryServiceApplication.class);

        app.setBanner((environment, sourceClass, out) -> {
            out.println("=== Eureka Discovery Service ===");
        });
    }
}
