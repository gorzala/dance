package net.dancier.dancer.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dancier.dancer.AbstractPostgreSQLEnabledTest;
import net.dancier.dancer.chat.client.ChatServiceClient;
import net.dancier.dancer.chat.dto.*;
import net.dancier.dancer.dancers.DancerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChatControllerTest extends AbstractPostgreSQLEnabledTest {

    @MockBean
    ChatServiceClient chatServiceClient;

    @Autowired
    DancerRepository dancerRepository;

    @Autowired
    ObjectMapper objectMapper;

    UUID userId = UUID.fromString("55bbf334-6649-11ed-8f65-5b299f0e161f");
    UUID chatId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    UUID dancerId = null;
    @BeforeEach
    void init() {
        this.dancerId = dancerRepository.findByUserId(userId).get().getId();
    }

    @Nested
    @DisplayName("GET /chats")
    public class GetChats {

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void getChatsShouldReturnChats() throws Exception {

            ChatDto chat = new ChatDto();
            chat.setParticipantIds(List.of(dancerId, UUID.randomUUID()));
            chat.setChatId(chatId);
            ChatDto[] chats = {chat};

            when(chatServiceClient.getChats(dancerId)).thenReturn(chats);

            ResultActions result = mockMvc
                    .perform(get("/chats"))
                    .andExpect(status().isOk());

            result.andExpect(jsonPath("$.[0].chatId").value(chatId.toString()));
        }
    }

    @Nested
    @DisplayName("POST /chats")
    public class PostChats {

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void postChatShouldReturnTheChat() throws Exception {
            List dancerIds = List.of(dancerId, UUID.randomUUID());
            CreateChatDto chat = new CreateChatDto();
            chat.setParticipantIds(dancerIds);

            ChatDto createdChat = new ChatDto();
            createdChat.setParticipantIds(dancerIds);
            createdChat.setChatId(UUID.randomUUID());

            CreatedChatDto createdChatDto = new CreatedChatDto();
            createdChatDto.id = UUID.randomUUID();

            when(chatServiceClient.createChat(chat)).thenReturn(createdChatDto);

            ResultActions result = mockMvc.perform(post("/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(chat)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));

            result.andExpect(jsonPath("$.id").isNotEmpty());
        }

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void postChatShouldNotCreateTheChatIfUserIsNotPartOfIt() throws Exception {
            List dancerIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            CreateChatDto chat = new CreateChatDto();
            chat.setParticipantIds(dancerIds);

            mockMvc.perform(post("/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(chat)))
                    .andExpect(status().isBadRequest());

        }
    }

    @Nested
    @DisplayName("GET /chats/id")
    public class GetChat {

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void getChatShouldReturnTheChat() throws Exception {
            UUID dancerId = dancerRepository.findByUserId(userId).get().getId();

            ChatDto chat = new ChatDto();
            chat.setParticipantIds(List.of(dancerId, UUID.randomUUID()));
            chat.setChatId(chatId);

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            ResultActions result = mockMvc.perform(
                    get("/chats/" + chatId)
            ).andExpect(status().isOk());

            result.andExpect(jsonPath("$.chatId").value(chatId.toString()));
        }

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void getChatShouldNotReturnTheChatIfUserIsNotPartOfIt() throws Exception {
            ChatDto chat = new ChatDto();
            chat.setParticipantIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
            chat.setChatId(chatId);

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(
                    get("/chats/" + chatId)
            ).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /chats/id/messages")
    public class GetMessages {

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void getMessagesShouldNotReturnMessagesIfUserIsNotInChat() throws Exception {
            ChatDto chat = new ChatDto();
            chat.setParticipantIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(
                            get("/chats/" + chatId + "/messages"))
                    .andExpect(status().isBadRequest());

        }

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void getMessagesShouldReturnMessagesIfUserIsInChat() throws Exception {
            UUID dancerId = dancerRepository.findByUserId(userId).get().getId();

            ChatDto chat = new ChatDto();
            chat.setParticipantIds(List.of(dancerId, UUID.randomUUID()));

            MessageDto message = new MessageDto();
            message.setText("Hallo");

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);
            when(chatServiceClient.getMessages(chatId, dancerId, Optional.empty())).thenReturn(new MessageDto[]{message});

            ResultActions result = mockMvc.perform(
                            get("/chats/" + chatId + "/messages"))
                    .andExpect(status().isOk());

            result.andExpect(jsonPath("$[0].text").value("Hallo"));

        }
    }

    @Nested
    @DisplayName("POST /chats/id/messages")
    public class PostMessages {

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void postMessagesShouldNotCreateTheMessageIfUserIsNotInTheChat() throws Exception {
            ChatDto chat = new ChatDto();
            chat.setParticipantIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

            CreateMessageDto message = new CreateMessageDto();
            message.setText("Hallo");

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(post("/chats/" + chatId + "/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(message))
            ).andExpect(status().isBadRequest());

            verify(chatServiceClient, times(0)).createMessage(any(), any());

        }

        @Test
        @WithUserDetails("user-with-a-profile@dancier.net")
        void postMessagesShouldCreateAMessage() throws Exception {
            ChatDto chat = new ChatDto();
            chat.setParticipantIds(List.of(dancerId, UUID.randomUUID()));

            CreateMessageDto message = new CreateMessageDto();
            message.setText("Hallo");

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(post("/chats/" + chatId + "/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(message))
            ).andExpect(status().isCreated());

        }
    }

}
