package com.chatapp.repository;

import com.chatapp.entity.FriendRequest;
import com.chatapp.entity.RequestStatus;
import com.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

    List<FriendRequest> findByReceiverAndStatus(User receiver, RequestStatus status);

    @Query("""
        SELECT fr FROM FriendRequest fr
        WHERE (fr.sender = :user OR fr.receiver = :user)
        AND fr.status = com.chatapp.entity.RequestStatus.ACCEPTED
    """)
    List<FriendRequest> findAcceptedByUser(@Param("user") User user);
}
