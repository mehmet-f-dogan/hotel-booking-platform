package dev.mehmetfd.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mehmetfd.common.dto.CreateReservationRequest;
import dev.mehmetfd.common.dto.UpdateReservationRequest;
import dev.mehmetfd.common.dto.RoomDto;
import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.reservation.model.Reservation;
import dev.mehmetfd.reservation.repository.ReservationRepository;
import dev.mehmetfd.reservation.service.LockService;
import dev.mehmetfd.reservation.service.ReservationEventProducer;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = ANY)
class ReservationControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RestTemplate restTemplate;

        @MockBean
        private LockService lockService;

        @MockBean
        private ReservationEventProducer reservationEventProducer;

        private MockedStatic<RequestContextHolder> mockedCtxHolder;

        @BeforeEach
        void setUp() {
                reservationRepository.deleteAll();

                willAnswer(invocation -> {
                        Runnable action = invocation.getArgument(1, Runnable.class);
                        action.run();
                        return null;
                }).given(lockService).executeWithLock(anyString(), any(Runnable.class));

                doNothing().when(reservationEventProducer).sendReservationEvent(any());
                doNothing().when(reservationEventProducer).sendReservationDeleteEvent(any());

                RequestContext ctx = mock(RequestContext.class);
                when(ctx.username()).thenReturn("integrationUser");
                mockedCtxHolder = mockStatic(RequestContextHolder.class);
                mockedCtxHolder.when(RequestContextHolder::get).thenReturn(ctx);
        }

        @AfterEach
        void tearDown() {
                if (mockedCtxHolder != null)
                        mockedCtxHolder.close();
        }

        @Test
        void createAndGetReservation_endToEnd() throws Exception {
                Long hotelId = 1L;
                Long roomId = 100L;
                LocalDate checkIn = LocalDate.now().plusDays(2);
                LocalDate checkOut = LocalDate.now().plusDays(4);

                RoomDto roomDto = new RoomDto(roomId, hotelId, "1", 1, BigDecimal.ONE, "admin", LocalDateTime.now(),
                                null);
                when(restTemplate.getForObject("http://HOTEL-SERVICE/rooms/" + roomId, RoomDto.class))
                                .thenReturn(roomDto);

                CreateReservationRequest req = new CreateReservationRequest(hotelId, roomId, "Guest1", checkIn,
                                checkOut);
                mockMvc.perform(post("/reservation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.roomId").value(roomId.intValue()))
                                .andExpect(jsonPath("$.guestName").value("Guest1"));

                Reservation saved = reservationRepository.findAll().stream().findFirst().orElseThrow();
                Assertions.assertEquals(hotelId, saved.getHotelId());
                Assertions.assertEquals(roomId, saved.getRoomId());

                mockMvc.perform(get("/reservations/" + saved.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.[0]?").doesNotExist());

                mockMvc.perform(get("/reservations/" + saved.getId()))
                                .andExpect(status().isOk());
        }

        @Test
        void createReservation_conflict_dueToOverlap() throws Exception {
                Long hotelId = 1L;
                Long roomId = 200L;
                LocalDate checkIn = LocalDate.now().plusDays(2);
                LocalDate checkOut = LocalDate.now().plusDays(4);

                RoomDto roomDto = new RoomDto(roomId, hotelId, "1", 1, BigDecimal.ONE, "admin", LocalDateTime.now(),
                                null);
                when(restTemplate.getForObject("http://HOTEL-SERVICE/rooms/" + roomId, RoomDto.class))
                                .thenReturn(roomDto);

                Reservation existing = new Reservation(hotelId, roomId, "Other", "someone", checkIn, checkOut);
                reservationRepository.saveAndFlush(existing);

                CreateReservationRequest req = new CreateReservationRequest(hotelId, roomId, "GuestX", checkIn,
                                checkOut);
                mockMvc.perform(post("/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void updateReservation_success() throws Exception {
                Reservation r = new Reservation(1L, 300L, "G", "acct", LocalDate.now().plusDays(5),
                                LocalDate.now().plusDays(7));
                r = reservationRepository.save(r);

                UpdateReservationRequest req = new UpdateReservationRequest(LocalDate.now().plusDays(8),
                                LocalDate.now().plusDays(10), "NewGuest");

                RoomDto roomDto = new RoomDto(r.getRoomId(), r.getHotelId(), "1", 1, BigDecimal.ONE, "admin",
                                LocalDateTime.now(), null);
                when(restTemplate.getForObject(anyString(), eq(RoomDto.class)))
                                .thenReturn(roomDto);

                mockMvc.perform(put("/reservations/" + r.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.guestName").value("NewGuest"));
        }

        @Test
        void deleteReservation_success() throws Exception {
                Reservation r = new Reservation(1L, 400L, "G", "acct", LocalDate.now().plusDays(2),
                                LocalDate.now().plusDays(3));
                r = reservationRepository.saveAndFlush(r);

                mockMvc.perform(delete("/reservations/" + r.getId()))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Deleted successfully"));

                assertTrue(reservationRepository.findById(r.getId()).isEmpty());
        }
}
