package com.umari.queueManager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Define o prefixo para quem está a "ouvir" (subscrever)
        // O frontend vai inscrever-se em: /topic/senhas
        config.enableSimpleBroker("/topic");

        // Prefixo para mensagens que vêm do cliente (se necessário no futuro)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cria o endpoint de conexão
        // O frontend conecta em: http://localhost:8080/ws-queue
        registry.addEndpoint("/ws-queue")
                .setAllowedOriginPatterns("*") // Permite conexões de qualquer origem (CORS)
                .withSockJS(); // Habilita fallback para browsers que não suportam WebSocket puro
    }
}