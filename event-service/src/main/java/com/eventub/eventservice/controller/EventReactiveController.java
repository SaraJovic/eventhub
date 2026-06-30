package com.eventub.eventservice.controller;

import com.eventub.eventservice.dto.EventResponse;
import com.eventub.eventservice.service.EventService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/events")
public class EventReactiveController {

    private final EventService eventService;

    public EventReactiveController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EventResponse> streamEvents() {
        return Flux.fromIterable(eventService.getAll())
                .delayElements(Duration.ofMillis(500));
    }
}
