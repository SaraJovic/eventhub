package com.eventub.userservice.service;

import com.eventub.userservice.domain.User;
import com.eventub.userservice.dto.UserCreateRequest;
import com.eventub.userservice.dto.UserResponse;
import com.eventub.userservice.dto.UserUpdateRequest;
import com.eventub.userservice.exception.EmailAlreadyExistsException;
import com.eventub.userservice.exception.UserNotFoundException;
import com.eventub.userservice.repository.UserRepository;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFullName("Ana Jovic");
        sampleUser.setEmail("ana@example.com");
        sampleUser.setRegisteredAt(LocalDateTime.now());
    }

    @Test
    void createUser_success() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(new UserCreateRequest("Ana Jovic", "ana@example.com"));

        assertThat(response.fullName()).isEqualTo("Ana Jovic");
        assertThat(response.email()).isEqualTo("ana@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> userService.createUser(new UserCreateRequest("Ana Jovic", "ana@example.com")))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ana@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("ana@example.com");
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("ana@example.com");
    }

    @Test
    void updateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUser(1L, new UserUpdateRequest("Ana Updated", "new@example.com"));

        verify(userRepository).save(any(User.class));
        assertThat(response).isNotNull();
    }

    @Test
    void updateUser_sameEmail_doesNotCheckDuplicate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.updateUser(1L, new UserUpdateRequest("Ana Updated", "ana@example.com"));

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void updateUser_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new UserUpdateRequest("X", "x@x.com")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_newEmailAlreadyTaken_throwsException() {
        User other = new User();
        other.setId(2L);
        other.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateUser(1L, new UserUpdateRequest("Ana", "taken@example.com")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }
}
