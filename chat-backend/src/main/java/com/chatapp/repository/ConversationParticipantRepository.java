package com.chatapp.repository;

import com.chatapp.entity.Conversation;
import com.chatapp.entity.ConversationParticipant;
import com.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    List<ConversationParticipant> findByUser(User user);

    Optional<ConversationParticipant> findByConversationAndUser(Conversation conversation, User user);

    List<ConversationParticipant> findByConversation(Conversation conversation);

    @Query("""
        SELECT cp1.conversation FROM ConversationParticipant cp1
        JOIN ConversationParticipant cp2 ON cp1.conversation = cp2.conversation
        WHERE cp1.user = :user1 AND cp2.user = :user2
        AND cp1.conversation.type = com.chatapp.entity.ConversationType.PRIVATE
    """)
    Optional<Conversation> findPrivateConversationBetween(@Param("user1") User user1, @Param("user2") User user2);
}
