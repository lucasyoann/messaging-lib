package com.marineyoann.messaging.controller;

import com.marineyoann.messaging.domain.Room;
import com.marineyoann.messaging.dto.AddMemberRequest;
import com.marineyoann.messaging.dto.CreateRoomRequest;
import com.marineyoann.messaging.dto.RoomDto;
import com.marineyoann.messaging.repository.RoomMemberRepository;
import com.marineyoann.messaging.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomMemberRepository memberRepository;
    private final Function<String, UUID> emailToUserId;

    public RoomController(RoomService roomService,
                          RoomMemberRepository memberRepository,
                          Function<String, UUID> emailToUserId) {
        this.roomService = roomService;
        this.memberRepository = memberRepository;
        this.emailToUserId = emailToUserId;
    }

    @PostMapping
    public ResponseEntity<RoomDto> create(@RequestBody CreateRoomRequest request, Principal principal) {
        UUID creatorId = emailToUserId.apply(principal.getName());
        Room room = roomService.createRoom(creatorId, request.type(), request.name(), request.memberIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(room));
    }

    @PostMapping("/{roomId}/members")
    public ResponseEntity<Void> addMember(@PathVariable UUID roomId,
                                          @RequestBody AddMemberRequest request,
                                          Principal principal) {
        UUID requesterId = emailToUserId.apply(principal.getName());

        if (!memberRepository.existsByRoomIdAndUserId(roomId, requesterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Salon introuvable");
        }

        roomService.addMember(roomId, request.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<RoomDto> myRooms(Principal principal) {
        UUID userId = emailToUserId.apply(principal.getName());
        return roomService.findRoomsForUser(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleValidationError(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    private RoomDto toDto(Room room) {
        return new RoomDto(room.getId(), room.getName(), room.getType(), room.getCreatedAt());
    }
}