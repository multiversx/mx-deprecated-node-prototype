package network.elrond.api.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ElrondWebSocketManager {


    private final SimpMessagingTemplate template;

    @Autowired
    public ElrondWebSocketManager(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void announce(String destination, String message) {
        this.template.convertAndSend(destination, message);
    }


}
