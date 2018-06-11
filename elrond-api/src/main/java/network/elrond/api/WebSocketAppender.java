package network.elrond.api;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.OutputStreamAppender;
import network.elrond.api.manager.ElrondWebSocketManager;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class WebSocketAppender<E> extends OutputStreamAppender<E> {

    private OutputStream outputStream = new ByteArrayOutputStream();

    private ElrondWebSocketManager elrondWebSocketManager;

    public WebSocketAppender() {

    }

    @Override
    public void start() {
        setOutputStream(outputStream);
        super.start();
    }

    @Override
    protected void append(E event) {
        String data = event + CoreConstants.LINE_SEPARATOR;
        elrondWebSocketManager.announce("/topic/public", data);

    }

    public ElrondWebSocketManager getElrondWebSocketManager() {
        return elrondWebSocketManager;
    }

    public void setElrondWebSocketManager(ElrondWebSocketManager elrondWebSocketManager) {
        this.elrondWebSocketManager = elrondWebSocketManager;
    }
}
