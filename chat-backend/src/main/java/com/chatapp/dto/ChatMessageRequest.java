package com.chatapp.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long conversationId;
    private String content;
}
