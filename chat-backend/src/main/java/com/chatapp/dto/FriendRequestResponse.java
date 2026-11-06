package com.chatapp.dto;

import lombok.*;

@Data @Builder
public class FriendRequestResponse {
    private Long id;
    private UserResponse sender;
    private UserResponse receiver;
    private String status;
}
