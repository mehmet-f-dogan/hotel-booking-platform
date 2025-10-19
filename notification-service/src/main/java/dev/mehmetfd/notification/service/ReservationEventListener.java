package dev.mehmetfd.notification.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import dev.mehmetfd.common.dto.ReservationEvent;

@Service
public class ReservationEventListener {

    @KafkaListener(topics = "reservation-events", groupId = "notification-group")
    public void consume(ConsumerRecord<String, ReservationEvent> record) {
        String reservationId = record.key();
        ReservationEvent event = record.value();

        if (event == null) {
            System.out.println("Reservation with ID: " + reservationId + " got deleted.");
        } else {
            System.out.println("Reservation ID: " + reservationId + " | " + event);
        }
    }
}