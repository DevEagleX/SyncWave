package com.chatapp.service;

import com.chatapp.dto.PresenceEvent;
import com.chatapp.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceService(UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void setOnline(String email) {
        updateStatus(email, "ONLINE");
    }

    public void setOffline(String email) {
        updateStatus(email, "OFFLINE");
    }

    private void updateStatus(String email, String status) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setStatus(status);
            user.setLastSeenAt(LocalDateTime.now());
            userRepository.save(user);

            messagingTemplate.convertAndSend("/topic/presence",
                    PresenceEvent.builder().userId(user.getId()).status(status).build());
        });
    }
}
