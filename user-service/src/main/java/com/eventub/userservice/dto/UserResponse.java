package com.eventub.userservice.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String fullName, String email, LocalDateTime registeredAt) {}
