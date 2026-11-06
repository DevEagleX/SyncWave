package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GroupConversationRequest {
    @NotBlank(message = "Group name is required")
    private String name;

    @NotEmpty(message = "Select at least one friend to add")
    private List<Long> participantIds;
}
