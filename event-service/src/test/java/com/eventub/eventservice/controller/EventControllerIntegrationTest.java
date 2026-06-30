package com.eventub.eventservice.controller;

import com.eventub.eventservice.repository.EventRepository;
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
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    private static final String VALID_EVENT_JSON = """
            {
              "title": "Spring Conference",
              "description": "Annual conference",
              "location": "Belgrade",
              "eventDate": "2030-06-15T10:00:00",
              "capacity": 100,
              "organizerId": 1
            }
            """;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void createEvent_returns201() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_EVENT_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Spring Conference"))
                .andExpect(jsonPath("$.location").value("Belgrade"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createEvent_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"location\":\"\",\"capacity\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getAllEvents_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getEventById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getEventsByOrganizer_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/events/organizer/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getUpcomingEvents_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/events/upcoming"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void fullCrudFlow() throws Exception {
        // Create
        String createResponse = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_EVENT_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String idStr = createResponse.replaceAll(".*\"id\":(\\d+).*", "$1");
        long id = Long.parseLong(idStr);

        // Get by id
        mockMvc.perform(get("/api/events/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Conference"));

        // Get by organizer
        mockMvc.perform(get("/api/events/organizer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Conference"));

        // Update
        mockMvc.perform(put("/api/events/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Conference\",\"location\":\"Novi Sad\",\"capacity\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Conference"));

        // Get all
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Updated Conference"));

        // Delete
        mockMvc.perform(delete("/api/events/" + id))
                .andExpect(status().isNoContent());

        // Confirm deleted
        mockMvc.perform(get("/api/events/" + id))
                .andExpect(status().isNotFound());
    }
}
