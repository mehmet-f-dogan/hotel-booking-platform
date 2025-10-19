package dev.mehmetfd.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import dev.mehmetfd.common.dto.ReservationEvent;

@Service
public class ReservationEventListener {

    @KafkaListener(topics = "reservation-events", groupId = "notification-group")
    public void consume(ReservationEvent event) {
        System.out.println("Received ReservationEvent: " + event);
    }
}