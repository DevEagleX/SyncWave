package com.chatapp.dto;

import lombok.*;

@Data @Builder
public class PresenceEvent {
    private Long userId;
    private String status;
}
