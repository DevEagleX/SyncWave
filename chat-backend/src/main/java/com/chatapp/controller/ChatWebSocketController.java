package com.chatapp.controller;

import com.chatapp.dto.*;
import com.chatapp.service.ConversationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ConversationService conversationService, SimpMessagingTemplate messagingTemplate) {
        this.conversationService = conversationService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(@Payload ChatMessageRequest request, Principal principal) {
        MessageResponse response = conversationService.saveMessage(principal.getName(), request);
        messagingTemplate.convertAndSend("/topic/conversations/" + request.getConversationId(), response);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingRequest request, Principal principal) {
        conversationService.assertParticipant(request.getConversationId(), principal.getName());

        var event = TypingEvent.builder()
                .conversationId(request.getConversationId())
                .user(conversationService.userResponseFor(principal.getName()))
                .typing(request.isTyping())
                .build();

        messagingTemplate.convertAndSend("/topic/conversations/" + request.getConversationId() + "/typing", event);
    }

    @MessageMapping("/chat.read")
    public void read(@Payload ReadRequest request, Principal principal) {
        conversationService.markRead(principal.getName(), request.getConversationId());

        var event = ReadEvent.builder()
                .conversationId(request.getConversationId())
                .userId(conversationService.userResponseFor(principal.getName()).getId())
                .readAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/conversations/" + request.getConversationId() + "/read", event);
    }
}
