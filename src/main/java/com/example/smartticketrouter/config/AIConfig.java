package com.example.smartticketrouter.config;

import com.example.smartticketrouter.ai.TicketAssistant;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public OpenAiChatModel chatModel(){
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4.1-mini")
                .build();
    }

    @Bean
    public TicketAssistant ticketAssistant(OpenAiChatModel chatModel){
        return AiServices.create(
                TicketAssistant.class,
                chatModel
        );
    }


}
