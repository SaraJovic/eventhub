package com.eventub.registrationservice.service;

import com.eventub.registrationservice.dto.RegistrationCreateRequest;
import com.eventub.registrationservice.dto.RegistrationResponse;

import java.util.List;

public interface RegistrationService {
    RegistrationResponse create(RegistrationCreateRequest request);
    RegistrationResponse getById(Long id);
    List<RegistrationResponse> getAll();
    List<RegistrationResponse> getByUser(Long userId);
    List<RegistrationResponse> getByEvent(Long eventId);
    RegistrationResponse cancel(Long id);
}
