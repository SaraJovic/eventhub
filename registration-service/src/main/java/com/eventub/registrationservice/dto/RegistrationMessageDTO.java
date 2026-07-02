package com.eventub.registrationservice.dto;

import java.time.LocalDateTime;

public record RegistrationMessageDTO(
        Long registrationId,
        Long userId,
        Long eventId,
        String status,
        LocalDateTime registeredAt
) {}
