package network.elrond.api;

import network.elrond.Application;
import network.elrond.ElrondFacade;
import network.elrond.ElrondFacadeImpl;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.crypto.PKSKPair;
import org.mapdb.Fun;
import network.elrond.p2p.PingResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;

@Component
class ElrondApiNode {

    private Application application;

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

    PKSKPair generatePublicKeyAndPrivateKey() {return getFacade().generatePublicKeyAndPrivateKey(); }

    PKSKPair generatePublicKeyFromPrivateKey(String privateKey) { return getFacade().generatePublicKeyFromPrivateKey(privateKey); }
}