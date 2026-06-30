package com.eventub.userservice.service;

import com.eventub.userservice.dto.UserCreateRequest;
import com.eventub.userservice.dto.UserResponse;
import com.eventub.userservice.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
