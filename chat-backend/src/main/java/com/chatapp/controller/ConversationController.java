package com.chatapp.controller;

import com.chatapp.dto.ConversationResponse;
import com.chatapp.dto.GroupConversationRequest;
import com.chatapp.dto.MessageResponse;
import com.chatapp.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/private/{friendId}")
    public ConversationResponse startPrivate(Authentication authentication, @PathVariable Long friendId) {
        return conversationService.getOrCreatePrivateConversation(authentication.getName(), friendId);
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationResponse> createGroup(Authentication authentication,
                                                              @Valid @RequestBody GroupConversationRequest request) {
        var response = conversationService.createGroupConversation(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ConversationResponse> list(Authentication authentication) {
        return conversationService.listConversations(authentication.getName());
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> messages(Authentication authentication, @PathVariable Long conversationId) {
        return conversationService.getMessages(authentication.getName(), conversationId);
    }

    @PutMapping("/{conversationId}/read")
    public ResponseEntity<Void> markRead(Authentication authentication, @PathVariable Long conversationId) {
        conversationService.markRead(authentication.getName(), conversationId);
        return ResponseEntity.ok().build();
    }
}
