package dev.mehmetfd.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.hotel.dto.RoomRequest;
import dev.mehmetfd.hotel.model.Hotel;
import dev.mehmetfd.hotel.model.Room;
import dev.mehmetfd.hotel.repository.HotelRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    private MockedStatic<RequestContextHolder> mockedCtxHolder;

    private Long hotelId;

    @BeforeEach
    void setup() {
        Hotel hotel = new Hotel();
        hotel.setName("Hotel IT");
        hotel.setAddress("Address IT");
        hotel.setStarRating(4);
        hotel.setCreatedBy("testUser");
        hotelRepository.save(hotel);
        hotelId = hotel.getId();

        RequestContext ctx = mock(RequestContext.class);
        when(ctx.username()).thenReturn("testUser");
        mockedCtxHolder = mockStatic(RequestContextHolder.class);
        mockedCtxHolder.when(RequestContextHolder::get).thenReturn(ctx);
    }

    @AfterEach
    void tearDown() {
        if (mockedCtxHolder != null)
            mockedCtxHolder.close();
    }

    @Test
    void testCreateAndGetRoom() throws Exception {
        RoomRequest request = new RoomRequest(hotelId, "101", 2, BigDecimal.TEN);

        String response = mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Room createdRoom = objectMapper.readValue(response, Room.class);

        mockMvc.perform(get("/rooms/" + createdRoom.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));
    }
}
