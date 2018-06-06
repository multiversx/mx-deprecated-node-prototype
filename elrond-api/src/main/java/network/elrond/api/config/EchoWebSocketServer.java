package network.elrond.api.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.ExceptionWebSocketHandlerDecorator;

import java.util.ArrayList;
import java.util.List;

@ComponentScan
@Controller
@EnableAutoConfiguration
@EnableWebSocket
public class EchoWebSocketServer implements WebSocketConfigurer {

    private HandlerMessage handler = new HandlerMessage();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler(), "/log").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler handler() {
        return new ExceptionWebSocketHandlerDecorator(handler);
    }

    public void sendToAll(String message){

        handler.sendToAll(message);
    }

//    public static void main(String[] args) throws Exception {
//        SpringApplication.run(EchoWebSocketServer.class, args);
//    }
}


