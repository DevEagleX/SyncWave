package com.chatapp.service;

import com.chatapp.dto.*;
import com.chatapp.entity.User;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getByEmail(email);

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(user);
        return toResponse(user);
    }

    public List<UserResponse> search(String query, String requesterEmail) {
        return userRepository.searchByNameOrEmail(query).stream()
                .filter(u -> !u.getEmail().equalsIgnoreCase(requesterEmail))
                .map(this::toResponse)
                .toList();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }
}
