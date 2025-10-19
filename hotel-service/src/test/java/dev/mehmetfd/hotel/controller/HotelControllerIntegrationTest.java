package dev.mehmetfd.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.hotel.dto.HotelRequest;
import dev.mehmetfd.hotel.model.Hotel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
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

@SpringBootTest
@AutoConfigureMockMvc
class HotelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<RequestContextHolder> mockedCtxHolder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
    void testCreateAndGetHotel() throws Exception {
        HotelRequest request = new HotelRequest("Hotel IT", "Address IT", 4);

        String response = mockMvc.perform(post("/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Hotel createdHotel = objectMapper.readValue(response, Hotel.class);

        mockMvc.perform(get("/hotels/" + createdHotel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hotel IT"));
    }
}
