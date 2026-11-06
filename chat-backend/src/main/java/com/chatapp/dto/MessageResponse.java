package com.chatapp.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private UserResponse sender;
    private String content;
    private LocalDateTime sentAt;
}
