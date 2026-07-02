package com.eventub.registrationservice.controller;

import com.eventub.registrationservice.dto.RegistrationCreateRequest;
import com.eventub.registrationservice.dto.RegistrationResponse;
import com.eventub.registrationservice.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> createRegistration(@Valid @RequestBody RegistrationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RegistrationResponse>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponse> getRegistrationById(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(registrationService.getByUser(userId));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.getByEvent(eventId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<RegistrationResponse> cancelRegistration(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.cancel(id));
    }
}
