package com.chatapp.controller;

import com.chatapp.dto.FriendRequestResponse;
import com.chatapp.dto.UserResponse;
import com.chatapp.entity.RequestStatus;
import com.chatapp.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/friend-requests/{receiverId}")
    public ResponseEntity<FriendRequestResponse> send(Authentication authentication, @PathVariable Long receiverId) {
        var response = friendService.sendRequest(authentication.getName(), receiverId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/friend-requests/{requestId}/accept")
    public FriendRequestResponse accept(Authentication authentication, @PathVariable Long requestId) {
        return friendService.respond(authentication.getName(), requestId, RequestStatus.ACCEPTED);
    }

    @PutMapping("/friend-requests/{requestId}/reject")
    public FriendRequestResponse reject(Authentication authentication, @PathVariable Long requestId) {
        return friendService.respond(authentication.getName(), requestId, RequestStatus.REJECTED);
    }

    @GetMapping("/friend-requests/incoming")
    public List<FriendRequestResponse> incoming(Authentication authentication) {
        return friendService.incomingRequests(authentication.getName());
    }

    @GetMapping("/friends")
    public List<UserResponse> friends(Authentication authentication) {
        return friendService.friendsList(authentication.getName());
    }
}
