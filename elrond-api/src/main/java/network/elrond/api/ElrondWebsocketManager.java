package network.elrond.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ElrondWebsocketManager {


    private final SimpMessagingTemplate template;

    @Autowired
    public ElrondWebsocketManager(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void announce(String destination, String message) {
        this.template.convertAndSend(destination, message);
//        switch (entity) {
//            case "log":
//                this.template.convertAndSend("log", message);
//
//                break;
//            default:
//                break;
//        }
    }


}
