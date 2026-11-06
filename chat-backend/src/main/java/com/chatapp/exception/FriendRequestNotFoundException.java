package com.chatapp.exception;

public class FriendRequestNotFoundException extends RuntimeException {
    public FriendRequestNotFoundException(Long id) {
        super("Friend request not found: " + id);
    }
}
