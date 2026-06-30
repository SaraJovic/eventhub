package com.eventub.eventservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventCreateRequest(
        @NotBlank(message = "Title must not be blank") String title,
        String description,
        @NotBlank(message = "Location must not be blank") String location,
        @NotNull(message = "Event date must not be null") LocalDateTime eventDate,
        @Min(value = 1, message = "Capacity must be greater than 0") int capacity,
        @NotNull(message = "Organizer ID must not be null") Long organizerId
) {}
