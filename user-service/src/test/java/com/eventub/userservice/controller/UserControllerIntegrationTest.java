package com.eventub.userservice.controller;

import com.eventub.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_returns201() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana Jovic\",\"email\":\"ana@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Ana Jovic"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.registeredAt").exists());
    }

    @Test
    void createUser_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana Jovic\",\"email\":\"dup@example.com\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana Jovic 2\",\"email\":\"dup@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createUser_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getAllUsers_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void fullCrudFlow() throws Exception {
        // Create
        String createResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test User\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract id by simple string parsing
        String idStr = createResponse.replaceAll(".*\"id\":(\\d+).*", "$1");
        long id = Long.parseLong(idStr);

        // Get by id
        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));

        // Update
        mockMvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Updated User\",\"email\":\"updated@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"));

        // Get all
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("updated@example.com"));

        // Delete
        mockMvc.perform(delete("/users/" + id))
                .andExpect(status().isNoContent());

        // Confirm deleted
        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }
}
