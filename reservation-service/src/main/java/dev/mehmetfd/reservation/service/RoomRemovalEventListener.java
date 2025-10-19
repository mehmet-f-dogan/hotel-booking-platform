package dev.mehmetfd.reservation.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import dev.mehmetfd.common.dto.RoomRemovalEvent;

@Service
public class RoomRemovalEventListener {

    @Autowired
    private ReservationService reservationService;

    @KafkaListener(topics = "room-events", groupId = "reservation-group")
    public void consume(ConsumerRecord<String, RoomRemovalEvent> record) {
        RoomRemovalEvent event = record.value();
        if (event != null) {
            reservationService.deleteRoomReservations(event.roomId());
        }
    }
}