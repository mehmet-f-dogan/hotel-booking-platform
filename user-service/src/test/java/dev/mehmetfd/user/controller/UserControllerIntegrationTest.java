package dev.mehmetfd.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mehmetfd.common.constants.Role;
import dev.mehmetfd.user.dto.CreateUserRequest;
import dev.mehmetfd.user.model.User;
import dev.mehmetfd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveUser() throws Exception {
        CreateUserRequest req = new CreateUserRequest("mehmet", "mypassword", Role.USER);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("mehmet"));

        User saved = userRepository.findByUsername("mehmet").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("mypassword");

        mockMvc.perform(get("/users/mehmet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mehmet"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturnConflictIfUserExists() throws Exception {
        User existing = new User();
        existing.setUsername("mehmet");
        existing.setPassword("encoded");
        existing.setRole(Role.ADMIN);
        userRepository.save(existing);

        CreateUserRequest req = new CreateUserRequest("mehmet", "secret", Role.USER);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
