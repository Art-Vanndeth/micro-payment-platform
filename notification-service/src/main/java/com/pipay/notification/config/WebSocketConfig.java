package com.pipay.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOrigins("http://127.0.0.1:8888/", "http://127.0.0.1:8095/")  // Be more specific in production
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(8192) // Message size limit
                .setSendBufferSizeLimit(8192) // Buffer size limit
                .setSendTimeLimit(10000); // Time limit to send a message in milliseconds
    }





//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        // Enable a simple message broker for in-memory message handling
//        config.enableSimpleBroker("/topic", "/queue");
//
//        // Set application destination prefix for client messages
//        config.setApplicationDestinationPrefixes("/app");
//
//        // Set user destination prefix for private messages
//        config.setUserDestinationPrefix("/user");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        // Register STOMP endpoint for WebSocket connections
//        registry.addEndpoint("/ws/admin")
//                .setAllowedOriginPatterns("*")
//                .withSockJS();
//
//        // Register endpoint without SockJS for direct WebSocket connections
//        registry.addEndpoint("/ws/admin")
//                .setAllowedOriginPatterns("*");
//    }
}
