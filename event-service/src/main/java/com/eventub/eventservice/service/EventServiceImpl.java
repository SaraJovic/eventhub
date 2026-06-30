package com.eventub.eventservice.service;

import com.eventub.eventservice.domain.Event;
import com.eventub.eventservice.dto.EventCreateRequest;
import com.eventub.eventservice.dto.EventResponse;
import com.eventub.eventservice.dto.EventUpdateRequest;
import com.eventub.eventservice.exception.EventNotFoundException;
import com.eventub.eventservice.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public EventResponse create(EventCreateRequest request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setEventDate(request.eventDate());
        event.setCapacity(request.capacity());
        event.setOrganizerId(request.organizerId());
        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getById(Long id) {
        return eventRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAll() {
        return eventRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcoming() {
        return eventRepository.findByEventDateAfter(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EventResponse update(Long id, EventUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        if (request.title() != null) event.setTitle(request.title());
        if (request.description() != null) event.setDescription(request.description());
        if (request.location() != null) event.setLocation(request.location());
        if (request.eventDate() != null) event.setEventDate(request.eventDate());
        if (request.capacity() > 0) event.setCapacity(request.capacity());
        if (request.organizerId() != null) event.setOrganizerId(request.organizerId());
        return toResponse(eventRepository.save(event));
    }

    @Override
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getEventDate(),
                event.getCapacity(),
                event.getOrganizerId(),
                event.getCreatedAt()
        );
    }
}
