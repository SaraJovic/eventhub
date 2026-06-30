package com.eventub.eventservice.service;

import com.eventub.eventservice.domain.Event;
import com.eventub.eventservice.dto.EventCreateRequest;
import com.eventub.eventservice.dto.EventResponse;
import com.eventub.eventservice.dto.EventUpdateRequest;
import com.eventub.eventservice.exception.EventNotFoundException;
import com.eventub.eventservice.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event sampleEvent;
    private final LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

    @BeforeEach
    void setUp() {
        sampleEvent = new Event();
        sampleEvent.setId(1L);
        sampleEvent.setTitle("Spring Conference");
        sampleEvent.setDescription("Annual Spring Boot conference");
        sampleEvent.setLocation("Belgrade");
        sampleEvent.setEventDate(futureDate);
        sampleEvent.setCapacity(100);
        sampleEvent.setOrganizerId(42L);
        sampleEvent.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void create_success() {
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);

        EventResponse response = eventService.create(new EventCreateRequest(
                "Spring Conference", "Annual Spring Boot conference", "Belgrade", futureDate, 100, 42L));

        assertThat(response.title()).isEqualTo("Spring Conference");
        assertThat(response.organizerId()).isEqualTo(42L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getById_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        EventResponse response = eventService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Spring Conference");
    }

    @Test
    void getById_notFound_throwsException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getById(99L))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_returnsList() {
        when(eventRepository.findAll()).thenReturn(List.of(sampleEvent));

        List<EventResponse> result = eventService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Spring Conference");
    }

    @Test
    void getByOrganizer_returnsList() {
        when(eventRepository.findByOrganizerId(42L)).thenReturn(List.of(sampleEvent));

        List<EventResponse> result = eventService.getByOrganizer(42L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).organizerId()).isEqualTo(42L);
    }

    @Test
    void getUpcoming_returnsFutureEvents() {
        when(eventRepository.findByEventDateAfter(any(LocalDateTime.class))).thenReturn(List.of(sampleEvent));

        List<EventResponse> result = eventService.getUpcoming();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).eventDate()).isAfter(LocalDateTime.now());
    }

    @Test
    void update_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);

        EventResponse response = eventService.update(1L,
                new EventUpdateRequest("Updated Title", null, "Novi Sad", futureDate, 200, null));

        verify(eventRepository).save(any(Event.class));
        assertThat(response).isNotNull();
    }

    @Test
    void update_notFound_throwsException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.update(99L,
                new EventUpdateRequest("X", null, "Y", futureDate, 10, null)))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void delete_success() {
        when(eventRepository.existsById(1L)).thenReturn(true);

        eventService.delete(1L);

        verify(eventRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsException() {
        when(eventRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> eventService.delete(99L))
                .isInstanceOf(EventNotFoundException.class);
        verify(eventRepository, never()).deleteById(any());
    }
}
