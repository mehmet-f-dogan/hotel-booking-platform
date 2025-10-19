package dev.mehmetfd.reservation.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import dev.mehmetfd.common.dto.ReservationEvent;

@Service
public class ReservationEventProducer {

    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;

    public ReservationEventProducer(KafkaTemplate<String, ReservationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReservationEvent(ReservationEvent event) {
        kafkaTemplate.send("reservation-events", event);
    }
}