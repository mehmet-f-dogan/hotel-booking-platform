package dev.mehmetfd.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mehmetfd.common.constants.Role;
import dev.mehmetfd.user.dto.CreateUserRequest;
import dev.mehmetfd.user.model.User;
import dev.mehmetfd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setup() {
        createUserRequest = new CreateUserRequest("mehmet", "password123", Role.USER);
    }

    @Test
    void createUser_ShouldReturn201_WhenUserIsCreated() throws Exception {
        when(userRepository.findByUsername("mehmet")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("mehmet"))
                .andExpect(jsonPath("$.role").value("USER"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isNotEqualTo("password123");
    }

    @Test
    void createUser_ShouldReturn409_WhenUsernameExists() throws Exception {
        when(userRepository.findByUsername("mehmet")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isConflict());

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_ShouldReturn200_WhenUserExists() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("mehmet");
        user.setPassword("encoded-pass");
        user.setRole(Role.ADMIN);
        when(userRepository.findByUsername("mehmet")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/mehmet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mehmet"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void getUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        when(userRepository.findByUsername("mehmet")).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/mehmet"))
                .andExpect(status().isNotFound());
    }
}
