package com.eventub.registrationservice.dto;

import com.eventub.registrationservice.domain.RegistrationStatus;

import java.time.LocalDateTime;

public record RegistrationResponse(
        Long id,
        Long userId,
        Long eventId,
        RegistrationStatus status,
        LocalDateTime registeredAt
) {}
