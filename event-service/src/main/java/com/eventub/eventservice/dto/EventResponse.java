package com.eventub.eventservice.dto;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String location,
        LocalDateTime eventDate,
        int capacity,
        Long organizerId,
        LocalDateTime createdAt
) {}
