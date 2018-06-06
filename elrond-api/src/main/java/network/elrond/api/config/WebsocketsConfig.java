package network.elrond.api.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.ExceptionWebSocketHandlerDecorator;

//@Configuration
////@EnableWebSocketMessageBroker
////public class WebsocketsConfig extends AbstractWebSocketMessageBrokerConfigurer {
////
////    @Override
////    public void registerStompEndpoints(StompEndpointRegistry registry) {
////        registry.addEndpoint("/socket")
////                .setAllowedOrigins("*")
////                .withSockJS();
////    }
////
////    @Override
////    public void configureMessageBroker(MessageBrokerRegistry registry) {
////        registry.setApplicationDestinationPrefixes("/app")
////                .enableSimpleBroker("/log");
////    }
////
////
////
////}

