package com.chatapp.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 100)
    private String fullName;

    @Size(max = 500)
    private String bio;

    @Size(max = 500)
    private String avatarUrl;
}
