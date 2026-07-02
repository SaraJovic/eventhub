package com.eventub.registrationservice.service;

import com.eventub.registrationservice.domain.Registration;
import com.eventub.registrationservice.domain.RegistrationStatus;
import com.eventub.registrationservice.dto.RegistrationCreateRequest;
import com.eventub.registrationservice.dto.RegistrationMessageDTO;
import com.eventub.registrationservice.dto.RegistrationResponse;
import com.eventub.registrationservice.exception.DuplicateRegistrationException;
import com.eventub.registrationservice.exception.RegistrationNotFoundException;
import com.eventub.registrationservice.messaging.RabbitMQProducer;
import com.eventub.registrationservice.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final RabbitMQProducer rabbitMQProducer;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository, RabbitMQProducer rabbitMQProducer) {
        this.registrationRepository = registrationRepository;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @Override
    public RegistrationResponse create(RegistrationCreateRequest request) {
        if (registrationRepository.existsByUserIdAndEventId(request.userId(), request.eventId())) {
            throw new DuplicateRegistrationException(
                    "User " + request.userId() + " is already registered for event " + request.eventId());
        }
        Registration registration = new Registration();
        registration.setUserId(request.userId());
        registration.setEventId(request.eventId());
        registration.setStatus(RegistrationStatus.CONFIRMED);
        Registration saved = registrationRepository.save(registration);
        rabbitMQProducer.sendRegistrationMessage(toMessage(saved));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponse getById(Long id) {
        return registrationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getAll() {
        return registrationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getByUser(Long userId) {
        return registrationRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RegistrationResponse cancel(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found with id: " + id));
        registration.setStatus(RegistrationStatus.CANCELLED);
        Registration saved = registrationRepository.save(registration);
        rabbitMQProducer.sendRegistrationMessage(toMessage(saved));
        return toResponse(saved);
    }

    private RegistrationMessageDTO toMessage(Registration registration) {
        return new RegistrationMessageDTO(
                registration.getId(),
                registration.getUserId(),
                registration.getEventId(),
                registration.getStatus().name(),
                registration.getRegisteredAt()
        );
    }

    private RegistrationResponse toResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getUserId(),
                registration.getEventId(),
                registration.getStatus(),
                registration.getRegisteredAt()
        );
    }
}
