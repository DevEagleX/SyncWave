package com.chatapp.dto;

import lombok.*;

@Data @Builder
public class ConversationResponse {
    private Long id;
    private String type;
    private String name;
    private UserResponse otherParticipant;
    private MessageResponse lastMessage;
    private long unreadCount;
}
