package network.elrond.api;

import ch.qos.logback.core.Appender;
import network.elrond.Application;
import network.elrond.ElrondFacade;
import network.elrond.ElrondFacadeImpl;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import org.mapdb.Fun;
import network.elrond.core.ByteArrayOutputStreamAppender;
import network.elrond.p2p.PingResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Component
class ElrondApiNode {

    private Application application;
    private ElrondWebsocketManager elrondWebsocketManager;


    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }


    private ElrondFacade getFacade() {
        return new ElrondFacadeImpl();
    }

    void start(AppContext context) {
        application = getFacade().start(context);

        MessageChannel messageChannel = new ExecutorSubscribableChannel();
        SimpMessagingTemplate simpMessagingTemplate = new SimpMessagingTemplate(messageChannel);
        elrondWebsocketManager = new ElrondWebsocketManager(simpMessagingTemplate);

        Thread threadPushWebSocket = new Thread(() -> {

            long oldSeconds = -1;
            long newSeconds;

            do{
                newSeconds = new Date().getTime() / 1000;

                if (newSeconds != oldSeconds){
                    oldSeconds = newSeconds;

                    //current second has changed, do something
                    //get list of appenders and iterate it

                    List<Appender> list = getFacade().getLoggerAppendersList();

                    for (int i = 0; i < list.size(); i++){
                        Appender appender = list.get(i);

                        if (appender.getClass().getName().compareToIgnoreCase(ByteArrayOutputStreamAppender.class.getName()) == 0) {
                            try {
                                elrondWebsocketManager.announce(appender.getName(), ((ByteArrayOutputStreamAppender) appender).toStringAndClear("UTF8"));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

                try{
                    Thread.sleep(1);
                } catch (Exception ex){
                    ex.printStackTrace();
                }

            } while (application.getState().isStillRunning());
        });
        threadPushWebSocket.start();

    }

    boolean stop() {
        return getFacade().stop(application);
    }

    BigInteger getBalance(AccountAddress address) {
        return getFacade().getBalance(address, application);
    }

    boolean send(AccountAddress receiver, BigInteger value) {
        return getFacade().send(receiver, value, application);
    }

    PingResponse ping(String ipAddress, int port) {return  getFacade().ping(ipAddress, port);}

    Fun.Tuple2<String, String> generatePublicKeyAndPrivateKey() {return getFacade().generatePublicKeyAndPrivateKey(); }

    Fun.Tuple2<String, String> generatePublicKeyFromPrivateKey(String privateKey) { return getFacade().generatePublicKeyFromPrivateKey(privateKey); }
}