package com.eventub.eventservice.repository;

import com.eventub.eventservice.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByEventDateAfter(LocalDateTime date);
}
