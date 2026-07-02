package com.eventub.registrationservice.dto;

import jakarta.validation.constraints.NotNull;

public record RegistrationCreateRequest(
        @NotNull(message = "User ID must not be null") Long userId,
        @NotNull(message = "Event ID must not be null") Long eventId
) {}
