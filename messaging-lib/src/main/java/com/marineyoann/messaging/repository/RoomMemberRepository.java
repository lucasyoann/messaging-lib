package com.marineyoann.messaging.repository;

import com.marineyoann.messaging.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMember.RoomMemberId> {

    /** Utilisé directement par le futur StompAuthChannelInterceptor pour autoriser une souscription/un envoi. */
    boolean existsByRoomIdAndUserId(UUID roomId, UUID userId);

    @Query("select m.userId from RoomMember m where m.roomId = :roomId")
    List<UUID> findUserIdsByRoomId(UUID roomId);

    @Query("select m.roomId from RoomMember m where m.userId = :userId")
    List<UUID> findRoomIdsByUserId(UUID userId);
}
