package network.elrond.api;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.OutputStreamAppender;
import network.elrond.api.config.EchoWebSocketServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WebSocketAppender<E> extends OutputStreamAppender<E> {
    //dummy output stream
    private OutputStream outputStream = new ByteArrayOutputStream();
    //private ElrondWebsocketManager elrondWebsocketManager;
    private EchoWebSocketServer echoWebSocketServer;

    public WebSocketAppender(){
    //    elrondWebsocketManager = null;
        echoWebSocketServer = null;
    }

    @Override
    public void start() {
        setOutputStream(outputStream);
        super.start();
    }

    @Override
    protected void append(E event){
        String data = event + CoreConstants.LINE_SEPARATOR;

        if (echoWebSocketServer != null){
            echoWebSocketServer.sendToAll(data);
        }
//        if (elrondWebsocketManager != null) {
//            elrondWebsocketManager.announce(this.getName(), data);
//        }
    }

    public void setEchoWebSocketServer(EchoWebSocketServer echoWebSocketServer){
        this.echoWebSocketServer = echoWebSocketServer;
    }

//    public ElrondWebsocketManager getElrondWebsocketManager(){
//        return (elrondWebsocketManager);
//    }
//
//    public void setElrondWebsocketManager(ElrondWebsocketManager elrondWebsocketManager){
//        this.elrondWebsocketManager = elrondWebsocketManager;
//    }

}
