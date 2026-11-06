package com.chatapp.service;

import com.chatapp.dto.FriendRequestResponse;
import com.chatapp.dto.UserResponse;
import com.chatapp.entity.*;
import com.chatapp.exception.FriendRequestNotFoundException;
import com.chatapp.exception.InvalidFriendRequestException;
import com.chatapp.repository.FriendRequestRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public FriendService(FriendRequestRepository friendRequestRepository,
                          UserRepository userRepository, UserService userService) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public FriendRequestResponse sendRequest(String senderEmail, Long receiverId) {
        User sender = userService.getByEmail(senderEmail);
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new InvalidFriendRequestException("User not found: " + receiverId));

        if (sender.getId().equals(receiver.getId())) {
            throw new InvalidFriendRequestException("You cannot send a friend request to yourself");
        }

        friendRequestRepository.findBySenderAndReceiver(sender, receiver).ifPresent(fr -> {
            throw new InvalidFriendRequestException("Friend request already exists");
        });

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build();

        friendRequestRepository.save(request);
        return toResponse(request);
    }

    public FriendRequestResponse respond(String actingUserEmail, Long requestId, RequestStatus newStatus) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestNotFoundException(requestId));

        if (!request.getReceiver().getEmail().equalsIgnoreCase(actingUserEmail)) {
            throw new AccessDeniedException("You are not authorized to respond to this request");
        }

        request.setStatus(newStatus);
        friendRequestRepository.save(request);
        return toResponse(request);
    }

    public List<FriendRequestResponse> incomingRequests(String email) {
        User user = userService.getByEmail(email);
        return friendRequestRepository.findByReceiverAndStatus(user, RequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    public List<UserResponse> friendsList(String email) {
        User user = userService.getByEmail(email);
        return friendRequestRepository.findAcceptedByUser(user).stream()
                .map(fr -> fr.getSender().getId().equals(user.getId()) ? fr.getReceiver() : fr.getSender())
                .map(userService::toResponse)
                .toList();
    }

    public boolean areFriends(User a, User b) {
        boolean aToB = friendRequestRepository.findBySenderAndReceiver(a, b)
                .filter(fr -> fr.getStatus() == RequestStatus.ACCEPTED).isPresent();
        boolean bToA = friendRequestRepository.findBySenderAndReceiver(b, a)
                .filter(fr -> fr.getStatus() == RequestStatus.ACCEPTED).isPresent();
        return aToB || bToA;
    }

    private FriendRequestResponse toResponse(FriendRequest fr) {
        return FriendRequestResponse.builder()
                .id(fr.getId())
                .sender(userService.toResponse(fr.getSender()))
                .receiver(userService.toResponse(fr.getReceiver()))
                .status(fr.getStatus().name())
                .build();
    }
}
