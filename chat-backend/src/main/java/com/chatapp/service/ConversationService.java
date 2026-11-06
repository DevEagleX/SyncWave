package com.chatapp.service;

import com.chatapp.dto.*;
import com.chatapp.entity.*;
import com.chatapp.exception.InvalidFriendRequestException;
import com.chatapp.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final FriendService friendService;

    public ConversationService(ConversationRepository conversationRepository,
                                ConversationParticipantRepository participantRepository,
                                MessageRepository messageRepository,
                                UserRepository userRepository,
                                UserService userService,
                                FriendService friendService) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.friendService = friendService;
    }

    public ConversationResponse getOrCreatePrivateConversation(String requesterEmail, Long friendId) {
        User requester = userService.getByEmail(requesterEmail);
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new InvalidFriendRequestException("User not found: " + friendId));

        if (!friendService.areFriends(requester, friend)) {
            throw new InvalidFriendRequestException("You can only start a chat with a friend");
        }

        Conversation conversation = participantRepository
                .findPrivateConversationBetween(requester, friend)
                .orElseGet(() -> createPrivateConversation(requester, friend));

        return toConversationResponse(conversation, requester);
    }

    private Conversation createPrivateConversation(User user1, User user2) {
        Conversation conversation = Conversation.builder().type(ConversationType.PRIVATE).build();
        conversationRepository.save(conversation);

        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(user1).build());
        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(user2).build());

        return conversation;
    }

    public ConversationResponse createGroupConversation(String creatorEmail, GroupConversationRequest request) {
        User creator = userService.getByEmail(creatorEmail);

        List<User> members = request.getParticipantIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new InvalidFriendRequestException("User not found: " + id)))
                .toList();

        for (User member : members) {
            if (!friendService.areFriends(creator, member)) {
                throw new InvalidFriendRequestException(
                        "You can only add friends to a group (not friends with: " + member.getEmail() + ")");
            }
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.GROUP)
                .name(request.getName())
                .build();
        conversationRepository.save(conversation);

        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(creator).build());
        for (User member : members) {
            participantRepository.save(ConversationParticipant.builder()
                    .conversation(conversation).user(member).build());
        }

        return toConversationResponse(conversation, creator);
    }

    public void markRead(String email, Long conversationId) {
        User user = userService.getByEmail(email);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new InvalidFriendRequestException("Conversation not found: " + conversationId));

        ConversationParticipant participant = participantRepository.findByConversationAndUser(conversation, user)
                .orElseThrow(() -> new AccessDeniedException("You are not a participant of this conversation"));

        participant.setLastReadAt(java.time.LocalDateTime.now());
        participantRepository.save(participant);
    }

    public void assertParticipant(Long conversationId, String email) {
        User user = userService.getByEmail(email);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new InvalidFriendRequestException("Conversation not found: " + conversationId));
        assertParticipant(conversation, user);
    }

    public UserResponse userResponseFor(String email) {
        return userService.toResponse(userService.getByEmail(email));
    }

    public List<ConversationResponse> listConversations(String email) {
        User user = userService.getByEmail(email);

        return participantRepository.findByUser(user).stream()
                .map(cp -> toConversationResponse(cp.getConversation(), user))
                .sorted(Comparator.comparing(
                        (ConversationResponse c) -> c.getLastMessage() != null ? c.getLastMessage().getSentAt() : null,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<MessageResponse> getMessages(String email, Long conversationId) {
        User user = userService.getByEmail(email);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new InvalidFriendRequestException("Conversation not found: " + conversationId));

        assertParticipant(conversation, user);

        return messageRepository.findByConversationOrderBySentAtAsc(conversation).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    public MessageResponse saveMessage(String senderEmail, ChatMessageRequest request) {
        User sender = userService.getByEmail(senderEmail);
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new InvalidFriendRequestException("Conversation not found: " + request.getConversationId()));

        assertParticipant(conversation, sender);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .build();

        messageRepository.save(message);
        return toMessageResponse(message);
    }

    private void assertParticipant(Conversation conversation, User user) {
        participantRepository.findByConversationAndUser(conversation, user)
                .orElseThrow(() -> new AccessDeniedException("You are not a participant of this conversation"));
    }

    private ConversationResponse toConversationResponse(Conversation conversation, User requestingUser) {
        UserResponse other = null;
        if (conversation.getType() == ConversationType.PRIVATE) {
            other = participantRepository.findByConversation(conversation).stream()
                    .map(ConversationParticipant::getUser)
                    .filter(u -> !u.getId().equals(requestingUser.getId()))
                    .findFirst()
                    .map(userService::toResponse)
                    .orElse(null);
        }

        Optional<Message> lastMessage = messageRepository.findTopByConversationOrderBySentAtDesc(conversation);

        // "Unread" = messages sent after the requesting user's own lastReadAt for this conversation.
        // A null lastReadAt means they've never opened it, so every message in it is unread.
        long unreadCount = participantRepository.findByConversationAndUser(conversation, requestingUser)
                .map(cp -> cp.getLastReadAt() == null
                        ? messageRepository.countByConversation(conversation)
                        : messageRepository.countByConversationAndSentAtAfter(conversation, cp.getLastReadAt()))
                .orElse(0L);

        return ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType().name())
                .name(conversation.getName())
                .otherParticipant(other)
                .lastMessage(lastMessage.map(this::toMessageResponse).orElse(null))
                .unreadCount(unreadCount)
                .build();
    }

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .sender(userService.toResponse(message.getSender()))
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}
