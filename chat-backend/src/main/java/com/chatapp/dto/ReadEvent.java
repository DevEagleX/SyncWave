package com.chatapp.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class ReadEvent {
    private Long conversationId;
    private Long userId;
    private LocalDateTime readAt;
}
