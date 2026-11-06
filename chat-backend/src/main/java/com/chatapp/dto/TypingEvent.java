package com.chatapp.dto;

import lombok.*;

@Data @Builder
public class TypingEvent {
    private Long conversationId;
    private UserResponse user;
    private boolean typing;
}
