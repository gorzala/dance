package net.dancier.dancer.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dancier.dancer.AbstractPostgreSQLEnabledTest;
import net.dancier.dancer.chat.client.ChatServiceClient;
import net.dancier.dancer.chat.dto.*;
import net.dancier.dancer.core.DancerRepository;
import net.dancier.dancer.core.model.Dancer;
import net.dancier.dancer.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
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
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ObjectMapper objectMapper;

    UUID userId = UUID.fromString("62ff5258-8976-11ec-b58c-e35f5b1fc926");
    UUID chatId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void init() {
        Dancer dancer = new Dancer();
        dancer.setUserId(userId);
        dancer.setDancerName("dancero");
        dancer.setCity("Dortmund");
        dancerRepository.save(dancer);
    }

    @Nested
    @DisplayName("GET /chats")
    public class GetChats {

        @Test
        @WithUserDetails("user@dancier.net")
        void getChatsShouldReturnChats() throws Exception {
            UUID dancerId = dancerRepository.findByUserId(userId).get().getId();
            
            ChatDto chat = new ChatDto();
            chat.setDancerIds(List.of(dancerId, UUID.randomUUID()));
            chat.setChatId(chatId);
            ChatsDto chats = new ChatsDto();
            chats.setChats(List.of(chat));

            when(chatServiceClient.getChats(dancerId)).thenReturn(chats);

            ResultActions result = mockMvc
                    .perform(get("/chats")
                            .cookie(getUserCookie()))
                    .andExpect(status().isOk());

            result.andExpect(jsonPath("$.chats[0].chatId").value(chatId.toString()));
        }
    }

    @Nested
    @DisplayName("POST /chats")
    public class PostChats {

        @Test
        @WithUserDetails("user@dancier.net")
        void postChatShouldReturnTheChat() throws Exception {
            UUID dancerId = dancerRepository.findByUserId(userId).get().getId();
            List dancerIds = List.of(dancerId, UUID.randomUUID());
            CreateChatDto chat = new CreateChatDto();
            chat.setDancerIds(dancerIds);
            chat.setType(ChatType.DIRECT);

            ChatDto createdChat = new ChatDto();
            createdChat.setDancerIds(dancerIds);
            createdChat.setType(ChatType.DIRECT);
            createdChat.setChatId(UUID.randomUUID());

            when(chatServiceClient.createChat(chat)).thenReturn(createdChat);

            ResultActions result = mockMvc.perform(post("/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(chat))
                            .cookie(getUserCookie()))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));

            result.andExpect(jsonPath("$.dancerIds").isNotEmpty());
            result.andExpect(jsonPath("$.chatId").isNotEmpty());
            result.andExpect(jsonPath("$.type").value(ChatType.DIRECT.name()));
        }

        @Test
        @WithUserDetails("user@dancier.net")
        void postChatShouldNotCreateTheChatIfUserIsNotPartOfIt() throws Exception {
            List dancerIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            CreateChatDto chat = new CreateChatDto();
            chat.setDancerIds(dancerIds);
            chat.setType(ChatType.DIRECT);

            mockMvc.perform(post("/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(chat))
                            .cookie(getUserCookie()))
                    .andExpect(status().isBadRequest());

        }
    }

    @Nested
    @DisplayName("GET /chats/id")
    public class GetChat {

        @Test
        @WithUserDetails("user@dancier.net")
        void getChatShouldReturnTheChat() throws Exception {
            UUID dancerId = dancerRepository.findByUserId(userId).get().getId();

            ChatDto chat = new ChatDto();
            chat.setDancerIds(List.of(dancerId, UUID.randomUUID()));
            chat.setChatId(chatId);

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            ResultActions result = mockMvc.perform(
                    get("/chats/" + chatId)
                            .cookie(getUserCookie())
            ).andExpect(status().isOk());

            result.andExpect(jsonPath("$.chatId").value(chatId.toString()));
        }

        @Test
        @WithUserDetails("user@dancier.net")
        void getChatShouldNotReturnTheChatIfUserIsNotPartOfIt() throws Exception {
            ChatDto chat = new ChatDto();
            chat.setDancerIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
            chat.setChatId(chatId);

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(
                    get("/chats/" + chatId)
                            .cookie(getUserCookie())
            ).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /chats/id/messages")
    public class GetMessages {

        @Test
        @WithUserDetails("user@dancier.net")
        void getMessagesShouldNotReturnMessagesIfUserIsNotInChat() throws Exception {
            ChatDto chat = new ChatDto();
            chat.setDancerIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(
                            get("/chats/" + chatId + "/messages")
                                    .cookie(getUserCookie()))
                    .andExpect(status().isBadRequest());

        }

        @Test
        @WithUserDetails("user@dancier.net")
        void getMessagesShouldReturnMessagesIfUserIsInChat() throws Exception {
            UUID dancerId = dancerRepository.findByUserId(userId).get().getId();

            ChatDto chat = new ChatDto();
            chat.setDancerIds(List.of(dancerId, UUID.randomUUID()));

            MessagesDto messages = new MessagesDto();
            MessageDto message = new MessageDto();
            message.setText("Hallo");
            messages.setMessages(List.of(message));

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);
            when(chatServiceClient.getMessages(chatId, dancerId, Optional.empty())).thenReturn(messages);

            ResultActions result = mockMvc.perform(
                            get("/chats/" + chatId + "/messages")
                                    .cookie(getUserCookie()))
                    .andExpect(status().isOk());

            result.andExpect(jsonPath("$.messages[0].text").value("Hallo"));

        }
    }

    @Nested
    @DisplayName("POST /chats/id/messages")
    public class PostMessages {

        @Test
        @WithUserDetails("user@dancier.net")
        void postMessagesShouldNotCreateTheMessageIfUserIsNotInTheChat() throws Exception {
            ChatDto chat = new ChatDto();
            chat.setDancerIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

            CreateMessageDto message = new CreateMessageDto();
            message.setText("Hallo");

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(post("/chats/" + chatId + "/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(message))
                    .cookie(getUserCookie())
            ).andExpect(status().isBadRequest());

            verify(chatServiceClient, times(0)).createMessage(any(), any());

        }

        @Test
        @WithUserDetails("user@dancier.net")
        void postMessagesShouldCreateAMessage() throws Exception {
            UUID dancerId = dancerRepository.findByUserId(userId).get().getId();

            ChatDto chat = new ChatDto();
            chat.setDancerIds(List.of(dancerId, UUID.randomUUID()));

            CreateMessageDto message = new CreateMessageDto();
            message.setText("Hallo");

            when(chatServiceClient.getChat(chatId)).thenReturn(chat);

            mockMvc.perform(post("/chats/" + chatId + "/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(message))
                    .cookie(getUserCookie())
            ).andExpect(status().isCreated());

        }
    }

    private Cookie getUserCookie() {
        return new Cookie("jwt-token", jwtTokenProvider.generateJwtToken(userId.toString()));
    }

}
