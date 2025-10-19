package dev.mehmetfd.reservation.service;

import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.common.dto.RoomDto;
import dev.mehmetfd.common.exception.BadRequestException;
import dev.mehmetfd.reservation.model.Reservation;
import dev.mehmetfd.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationServiceTest {

    @Mock
    private LockService lockService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationEventProducer reservationEventProducer;

    @Mock
    private RestTemplate restTemplate;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(lockService, reservationRepository, reservationEventProducer,
                restTemplate);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(lockService).executeWithLock(anyString(), any(Runnable.class));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(lockService).executeWithLock(anyString(), any(Runnable.class), anyLong(), anyInt(), anyLong());
    }

    @Test
    void createReservation_successfulFlow_savesReservationAndSendsEvent() {
        Long hotelId = 1L;
        Long roomId = 10L;
        String guestName = "John Doe";
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(5);

        RoomDto roomDto = new RoomDto(roomId, hotelId, "1", 1, BigDecimal.ONE, "admin", LocalDateTime.now(), null);
        when(restTemplate.getForObject(eq("http://HOTEL-SERVICE/rooms/" + roomId), eq(RoomDto.class)))
                .thenReturn(roomDto);

        when(reservationRepository.findOverlappingReservations(eq(roomId), eq(checkIn), eq(checkOut)))
                .thenReturn(Collections.emptyList());

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        RequestContext mockCtx = mock(RequestContext.class);
        when(mockCtx.username()).thenReturn("accountUser");
        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::get).thenReturn(mockCtx);

            Reservation saved = reservationService.createReservation(hotelId, roomId, guestName, checkIn, checkOut);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo(100L);
            assertThat(saved.getRoomId()).isEqualTo(roomId);
            assertThat(saved.getHotelId()).isEqualTo(hotelId);
            assertThat(saved.getGuestName()).isEqualTo(guestName);
            verify(reservationEventProducer).sendReservationEvent(any());
            verify(reservationRepository).save(any(Reservation.class));
        }
    }

    @Test
    void createReservation_roomServiceThrows_resourceNotFoundException() {
        Long hotelId = 1L;
        Long roomId = 10L;
        String guestName = "John";
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        when(restTemplate.getForObject(anyString(), eq(RoomDto.class)))
                .thenThrow(new RestClientException("not found"));

        assertThatThrownBy(() -> reservationService.createReservation(hotelId, roomId, guestName, checkIn, checkOut))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    void createReservation_roomHotelMismatch_throwsResourceNotFound() {
        Long hotelId = 1L;
        Long roomId = 10L;
        String guestName = "John";
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        RoomDto roomDto = new RoomDto(roomId, 999L, "1", 1, BigDecimal.ONE, "admin", LocalDateTime.now(), null);

        when(restTemplate.getForObject(anyString(), eq(RoomDto.class))).thenReturn(roomDto);

        assertThatThrownBy(() -> reservationService.createReservation(hotelId, roomId, guestName, checkIn, checkOut))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    void createReservation_withOverlappingReservation_throwsBadRequestException() {
        Long hotelId = 1L;
        Long roomId = 10L;
        String guestName = "John";
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(8);

        RoomDto roomDto = new RoomDto(roomId, hotelId, "1", 1, BigDecimal.ONE, "admin", LocalDateTime.now(), null);

        when(restTemplate.getForObject(anyString(), eq(RoomDto.class))).thenReturn(roomDto);

        Reservation existing = new Reservation(hotelId, roomId, "Other", "someone", checkIn.plusDays(1),
                checkOut.plusDays(1));
        when(reservationRepository.findOverlappingReservations(eq(roomId), eq(checkIn), eq(checkOut)))
                .thenReturn(Collections.singletonList(existing));

        RequestContext mockCtx = mock(RequestContext.class);
        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::get).thenReturn(mockCtx);

            assertThatThrownBy(
                    () -> reservationService.createReservation(hotelId, roomId, guestName, checkIn, checkOut))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void updateReservation_success_updatesSavedReservationAndSendsEvent() {
        Long id = 5L;
        Long hotelId = 1L;
        Long roomId = 10L;
        Reservation existing = new Reservation(hotelId, roomId, "Old Guest", "account", LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6));
        existing.setId(id);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.findOverlappingReservations(eq(roomId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestContext mockCtx = mock(RequestContext.class);
        when(mockCtx.username()).thenReturn("accountUser");
        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::get).thenReturn(mockCtx);

            LocalDate newCheckIn = LocalDate.now().plusDays(7);
            LocalDate newCheckOut = LocalDate.now().plusDays(9);
            Reservation updated = reservationService.updateReservation(id, newCheckIn, newCheckOut, "New Guest");

            assertThat(updated.getGuestName()).isEqualTo("New Guest");
            assertThat(updated.getCheckInDate()).isEqualTo(newCheckIn);
            verify(reservationEventProducer).sendReservationEvent(any());
            verify(reservationRepository).save(existing);
        }
    }

    @Test
    void updateReservation_notFound_throwsIllegalArgumentException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateReservation(999L, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation not found");
    }

    @Test
    void deleteReservation_success_deletesAndSendsDeleteEvent() {
        Long id = 7L;
        Reservation r = new Reservation(1L, 20L, "G", "account", LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3));
        r.setId(id);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(r));
        doNothing().when(reservationRepository).deleteById(id);

        reservationService.deleteReservation(id);

        verify(reservationRepository).deleteById(id);
        verify(reservationEventProducer).sendReservationDeleteEvent(id);
    }

    @Test
    void deleteReservation_notFound_throwsIllegalArgumentException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation not found");
    }

    @Test
    void validateDates_pastDates_throwsIllegalArgumentException() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Long hotelId = 1L;
        Long roomId = 1L;
        RoomDto roomDto = new RoomDto(roomId, hotelId, "1", 1, BigDecimal.ONE, "admin", LocalDateTime.now(), null);
        when(restTemplate.getForObject(anyString(), eq(RoomDto.class))).thenReturn(roomDto);

        RequestContext mockCtx = mock(RequestContext.class);
        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::get).thenReturn(mockCtx);
            assertThatThrownBy(() -> reservationService.createReservation(hotelId, roomId, "g", yesterday, tomorrow))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be in the past");
        }
    }

    @Test
    void validateDates_checkoutNotAfter_checkIn_throwsIllegalArgumentException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(5);

        Long hotelId = 1L;
        Long roomId = 1L;
        RoomDto roomDto = new RoomDto(roomId, hotelId, "1", 1, BigDecimal.ONE, "admin", LocalDateTime.now(), null);
        when(restTemplate.getForObject(anyString(), eq(RoomDto.class))).thenReturn(roomDto);

        RequestContext mockCtx = mock(RequestContext.class);
        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::get).thenReturn(mockCtx);
            assertThatThrownBy(() -> reservationService.createReservation(hotelId, roomId, "g", checkIn, checkOut))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Check-out must be after check-in");
        }
    }
}
