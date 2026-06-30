package com.eventub.eventservice.service;

import com.eventub.eventservice.dto.EventCreateRequest;
import com.eventub.eventservice.dto.EventResponse;
import com.eventub.eventservice.dto.EventUpdateRequest;

import java.util.List;

public interface EventService {
    EventResponse create(EventCreateRequest request);
    EventResponse getById(Long id);
    List<EventResponse> getAll();
    List<EventResponse> getByOrganizer(Long organizerId);
    List<EventResponse> getUpcoming();
    EventResponse update(Long id, EventUpdateRequest request);
    void delete(Long id);
}
