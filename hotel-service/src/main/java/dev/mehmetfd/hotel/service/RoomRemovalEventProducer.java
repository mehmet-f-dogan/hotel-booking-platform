package dev.mehmetfd.hotel.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import dev.mehmetfd.common.dto.RoomRemovalEvent;

@Service
public class RoomRemovalEventProducer {

    private final KafkaTemplate<String, RoomRemovalEvent> kafkaTemplate;

    public RoomRemovalEventProducer(KafkaTemplate<String, RoomRemovalEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRoomRemovalEvent(RoomRemovalEvent event) {
        kafkaTemplate.send("room-events", event.roomId() + "", event);
    }
}
