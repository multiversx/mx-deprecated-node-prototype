package network.elrond.api.config;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

public class HandlerMessage implements WebSocketHandler {
    private List<WebSocketSession> listSessions = new ArrayList<WebSocketSession>();
    private Object lockList = new Object();

    @Override
    public void handleMessage(WebSocketSession session,
                              WebSocketMessage<?> encodedMessage) throws Exception {
        //not used

//        if (encodedMessage instanceof org.springframework.web.socket.TextMessage) {
//            org.springframework.web.socket.TextMessage castedTextMessage = (org.springframework.web.socket.TextMessage) encodedMessage;
//            String message = castedTextMessage.getPayload();
//            session.sendMessage(new org.springframework.web.socket.TextMessage(message));
//            System.out.println(message);
//        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession arg0, CloseStatus arg1)
            throws Exception {
        synchronized (lockList) {
            if (listSessions.contains(arg0)) {
                listSessions.remove(arg0);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession arg0)
            throws Exception {
        org.springframework.web.socket.TextMessage message =
                new org.springframework.web.socket.TextMessage("connected");

        synchronized (lockList) {
            listSessions.add(arg0);
        }

//        Thread thr = new Thread(() -> {
//            do{
//                try {
//                    arg0.sendMessage(new org.springframework.web.socket.TextMessage("ping"));
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//                try{
//                    Thread.sleep(1000);
//                }catch (Exception ex){
//                    ex.printStackTrace();
//                }
//            } while(true);
//        });
//        thr.start();
    }

    @Override
    public void handleTransportError(WebSocketSession arg0, Throwable arg1)
            throws Exception {
        synchronized (lockList) {
            if (listSessions.contains(arg0)) {
                listSessions.remove(arg0);
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendToAll(String message) {
        synchronized (lockList) {
            for (WebSocketSession arg : listSessions) {
                try {
                    arg.sendMessage(new org.springframework.web.socket.TextMessage(message));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}

