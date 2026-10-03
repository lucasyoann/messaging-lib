package com.marineyoann.messaging.service;

import com.marineyoann.messaging.domain.Room;
import com.marineyoann.messaging.domain.RoomMember;
import com.marineyoann.messaging.domain.RoomType;
import com.marineyoann.messaging.repository.RoomMemberRepository;
import com.marineyoann.messaging.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository memberRepository;

    public RoomService(RoomRepository roomRepository, RoomMemberRepository memberRepository) {
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Room createRoom(UUID creatorId, RoomType type, String name, List<UUID> requestedMemberIds) {
        // le créateur est ajouté séparément comme OWNER, on l'exclut ici pour éviter un doublon
        List<UUID> otherMemberIds = requestedMemberIds.stream()
                .filter(id -> !id.equals(creatorId))
                .distinct()
                .toList();

        Room room;
        if (type == RoomType.DIRECT) {
            if (otherMemberIds.size() != 1) {
                throw new IllegalArgumentException("Une conversation directe doit avoir exactement un autre participant");
            }
            room = Room.newDirect();
        } else {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Un salon de groupe doit avoir un nom");
            }
            if (otherMemberIds.isEmpty()) {
                throw new IllegalArgumentException("Un salon de groupe doit avoir au moins un membre en plus du créateur");
            }
            room = Room.newGroup(name);
        }

        roomRepository.save(room);
        memberRepository.save(RoomMember.owner(room.getId(), creatorId));
        for (UUID memberId : otherMemberIds) {
            memberRepository.save(RoomMember.member(room.getId(), memberId));
        }

        return room;
    }

    @Transactional
    public void addMember(UUID roomId, UUID newMemberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Salon introuvable"));

        if (room.getType() == RoomType.DIRECT) {
            throw new IllegalStateException("Impossible d'ajouter un membre à une conversation directe");
        }

        if (memberRepository.existsByRoomIdAndUserId(roomId, newMemberId)) {
            return; // déjà membre : idempotent, pas une erreur
        }

        memberRepository.save(RoomMember.member(roomId, newMemberId));
    }

    public List<Room> findRoomsForUser(UUID userId) {
        List<UUID> roomIds = memberRepository.findRoomIdsByUserId(userId);
        return roomRepository.findAllById(roomIds);
    }
}