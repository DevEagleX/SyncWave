package com.chatapp.dto;

import lombok.*;

@Data @Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String status;
}
