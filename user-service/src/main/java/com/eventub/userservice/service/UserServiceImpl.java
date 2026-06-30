package com.eventub.userservice.service;

import com.eventub.userservice.domain.User;
import com.eventub.userservice.dto.UserCreateRequest;
import com.eventub.userservice.dto.UserResponse;
import com.eventub.userservice.dto.UserUpdateRequest;
import com.eventub.userservice.exception.EmailAlreadyExistsException;
import com.eventub.userservice.exception.UserNotFoundException;
import com.eventub.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new EmailAlreadyExistsException("Email already in use: " + request.email());
        });
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (!user.getEmail().equals(request.email())) {
            userRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new EmailAlreadyExistsException("Email already in use: " + request.email());
            });
        }
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        return toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRegisteredAt());
    }
}
