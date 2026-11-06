package com.chatapp.dto;

import lombok.*;

@Data @Builder
public class AuthResponse {
    private String token;
    private UserResponse user;
}
