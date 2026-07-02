package com.eventub.registrationservice.repository;

import com.eventub.registrationservice.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserId(Long userId);
    List<Registration> findByEventId(Long eventId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
