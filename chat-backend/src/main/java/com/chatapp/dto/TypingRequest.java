package com.chatapp.dto;

import lombok.Data;

@Data
public class TypingRequest {
    private Long conversationId;
    private boolean typing;
}
