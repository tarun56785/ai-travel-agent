package com.concierge.ai_travel_agent.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TravelAgentController {

    private final ChatClient chatClient;

    public TravelAgentController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a helpful, professional global travel concierge. You have access to tools to find live weather and flights. Keep your answers concise and human-like.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                // .defaultTools("getWeather", "searchFlights", "finalizeTrip") // Ensure beans with these names exist in your configuration
                .build();
    }

    @GetMapping(value = "/api/secure/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message, Authentication authentication) {

        String username = authentication.getName();

        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, username))
                .stream()
                .content();
    }
}
