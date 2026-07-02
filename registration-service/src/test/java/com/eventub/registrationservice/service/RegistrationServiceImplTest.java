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
class RegistrationServiceImplTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private Registration sampleRegistration;

    @BeforeEach
    void setUp() {
        sampleRegistration = new Registration();
        sampleRegistration.setId(1L);
        sampleRegistration.setUserId(10L);
        sampleRegistration.setEventId(20L);
        sampleRegistration.setStatus(RegistrationStatus.CONFIRMED);
        sampleRegistration.setRegisteredAt(LocalDateTime.now());
    }

    @Test
    void create_success() {
        when(registrationRepository.existsByUserIdAndEventId(10L, 20L)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenReturn(sampleRegistration);

        RegistrationResponse response = registrationService.create(new RegistrationCreateRequest(10L, 20L));

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.eventId()).isEqualTo(20L);
        assertThat(response.status()).isEqualTo(RegistrationStatus.CONFIRMED);
        verify(registrationRepository).save(any(Registration.class));
        verify(rabbitMQProducer).sendRegistrationMessage(any(RegistrationMessageDTO.class));
    }

    @Test
    void create_duplicate_throwsException() {
        when(registrationRepository.existsByUserIdAndEventId(10L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.create(new RegistrationCreateRequest(10L, 20L)))
                .isInstanceOf(DuplicateRegistrationException.class);

        verify(registrationRepository, never()).save(any());
        verify(rabbitMQProducer, never()).sendRegistrationMessage(any());
    }

    @Test
    void getById_success() {
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(sampleRegistration));

        RegistrationResponse response = registrationService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(10L);
    }

    @Test
    void getById_notFound_throwsException() {
        when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.getById(99L))
                .isInstanceOf(RegistrationNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_returnsList() {
        when(registrationRepository.findAll()).thenReturn(List.of(sampleRegistration));

        List<RegistrationResponse> result = registrationService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(10L);
    }

    @Test
    void getByUser_returnsList() {
        when(registrationRepository.findByUserId(10L)).thenReturn(List.of(sampleRegistration));

        List<RegistrationResponse> result = registrationService.getByUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(10L);
    }

    @Test
    void getByEvent_returnsList() {
        when(registrationRepository.findByEventId(20L)).thenReturn(List.of(sampleRegistration));

        List<RegistrationResponse> result = registrationService.getByEvent(20L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).eventId()).isEqualTo(20L);
    }

    @Test
    void cancel_success() {
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(sampleRegistration));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationResponse response = registrationService.cancel(1L);

        assertThat(response.status()).isEqualTo(RegistrationStatus.CANCELLED);
        verify(registrationRepository).save(any(Registration.class));
        verify(rabbitMQProducer).sendRegistrationMessage(any(RegistrationMessageDTO.class));
    }

    @Test
    void cancel_notFound_throwsException() {
        when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.cancel(99L))
                .isInstanceOf(RegistrationNotFoundException.class);

        verify(registrationRepository, never()).save(any());
        verify(rabbitMQProducer, never()).sendRegistrationMessage(any());
    }
}
