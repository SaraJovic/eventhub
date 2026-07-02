package com.eventub.registrationservice.controller;

import com.eventub.registrationservice.messaging.RabbitMQProducer;
import com.eventub.registrationservice.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationRepository registrationRepository;

    @MockitoBean
    private RabbitMQProducer rabbitMQProducer;

    private static final String VALID_REGISTRATION_JSON = """
            {
              "userId": 10,
              "eventId": 20
            }
            """;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        doNothing().when(rabbitMQProducer).sendRegistrationMessage(any());
    }

    @Test
    void createRegistration_returns201() throws Exception {
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTRATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.eventId").value(20))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.registeredAt").exists());
    }

    @Test
    void createRegistration_duplicate_returns409() throws Exception {
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTRATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTRATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createRegistration_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getAllRegistrations_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/registrations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getRegistrationById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/registrations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getRegistrationsByUser_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/registrations/user/10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getRegistrationsByEvent_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/registrations/event/20"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void fullRegistrationFlow() throws Exception {
        // Create
        String createResponse = mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTRATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String idStr = createResponse.replaceAll(".*\"id\":(\\d+).*", "$1");
        long id = Long.parseLong(idStr);

        // Get by id
        mockMvc.perform(get("/api/registrations/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Get by user
        mockMvc.perform(get("/api/registrations/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10));

        // Get by event
        mockMvc.perform(get("/api/registrations/event/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(20));

        // Cancel
        mockMvc.perform(put("/api/registrations/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Get all reflects cancellation
        mockMvc.perform(get("/api/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    void cancelRegistration_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/registrations/999/cancel"))
                .andExpect(status().isNotFound());
    }
}
