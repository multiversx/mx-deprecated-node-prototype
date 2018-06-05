package network.elrond.api;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.OutputStreamAppender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WebSocketAppender<E> extends OutputStreamAppender<E> {
    //dummy output stream
    private OutputStream outputStream = new ByteArrayOutputStream();
    private ElrondWebsocketManager elrondWebsocketManager;

    public WebSocketAppender(){
        elrondWebsocketManager = null;
    }

    @Override
    public void start() {
        setOutputStream(outputStream);
        super.start();
    }

    @Override
    protected void writeOut(E event) throws IOException {
        String data = event + CoreConstants.LINE_SEPARATOR;

        if (elrondWebsocketManager != null) {
            elrondWebsocketManager.announce(this.getName(), data);
        }
    }

    public ElrondWebsocketManager getElrondWebsocketManager(){
        return (elrondWebsocketManager);
    }

    public void setElrondWebsocketManager(ElrondWebsocketManager elrondWebsocketManager){
        this.elrondWebsocketManager = elrondWebsocketManager;
    }

}
