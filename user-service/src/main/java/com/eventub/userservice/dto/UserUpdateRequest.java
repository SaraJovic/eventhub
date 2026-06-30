package com.eventub.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank(message = "Full name must not be blank") String fullName,
        @NotBlank(message = "Email must not be blank") @Email(message = "Email must be valid") String email
) {}
