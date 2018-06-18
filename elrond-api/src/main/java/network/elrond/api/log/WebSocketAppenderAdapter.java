package network.elrond.api.log;


import network.elrond.api.manager.ElrondWebSocketManager;

import java.io.Serializable;


public class WebSocketAppenderAdapter implements Serializable {


    private ElrondWebSocketManager elrondWebSocketManager;

    private static WebSocketAppenderAdapter instance = new WebSocketAppenderAdapter();

    public static WebSocketAppenderAdapter instance() {
        return instance;
    }

    public ElrondWebSocketManager getElrondWebSocketManager() {
        return elrondWebSocketManager;
    }

    public void setElrondWebSocketManager(ElrondWebSocketManager elrondWebSocketManager) {
        this.elrondWebSocketManager = elrondWebSocketManager;
    }
}
