package com.marineyoann.messaging.service;

import com.marineyoann.messaging.domain.OutboxEvent;
import com.marineyoann.messaging.repository.MessageRepository;
import com.marineyoann.messaging.repository.MessageStatusRepository;
import com.marineyoann.messaging.repository.OutboxEventRepository;
import com.marineyoann.messaging.repository.RoomMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    MessageRepository messageRepository;
    @Mock
    MessageStatusRepository statusRepository;
    @Mock
    RoomMemberRepository memberRepository;
    @Mock
    OutboxEventRepository outboxRepository;

    ChatService chatService;

    @BeforeEach
    void setUp() {

        JsonMapper jsonMapper = JsonMapper.builder().build();

        chatService = new ChatService(messageRepository, statusRepository,
                memberRepository, outboxRepository, jsonMapper);
    }

    @Test
    void sendMessage_creeUnStatutParDestinataireSaufExpediteur() {
        UUID roomId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID otherMember = UUID.randomUUID();

        when(memberRepository.findUserIdsByRoomId(roomId))
                .thenReturn(List.of(senderId, otherMember)); // 2 membres, dont l'expéditeur

        chatService.sendMessage(roomId, senderId, "Salut !");

        // un seul statut créé, pour l'autre membre, pas pour l'expéditeur
        verify(statusRepository, times(1)).save(argThat(status -> status.getUserId().equals(otherMember)));
        verify(outboxRepository, times(1)).save(any(OutboxEvent.class));
    }

}