package com.chatapp.repository;

import com.chatapp.entity.Conversation;
import com.chatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
    Optional<Message> findTopByConversationOrderBySentAtDesc(Conversation conversation);

    // Used for the unread badge - counts messages sent after the user's lastReadAt.
    long countByConversationAndSentAtAfter(Conversation conversation, LocalDateTime sentAtAfter);

    // Overload for a conversation the user has NEVER opened (lastReadAt is null) - every message counts.
    long countByConversation(Conversation conversation);
}
