package network.elrond.api;

import network.elrond.Application;
import network.elrond.ElrondFacade;
import network.elrond.ElrondFacadeImpl;
import network.elrond.account.AccountAddress;
import network.elrond.api.log.WebSocketAppenderAdapter;
import network.elrond.api.manager.ElrondWebSocketManager;
import network.elrond.application.AppContext;
import network.elrond.crypto.PKSKPair;
import network.elrond.data.BootstrapType;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;
import network.elrond.p2p.PingResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

@Component
class ElrondApiNode {

    @Autowired
    private ElrondWebSocketManager elrondWebSocketManager;

    private Application application;

    public Application getApplication() {
        return application;
    }

    private ElrondFacade getFacade() {
        return new ElrondFacadeImpl();
    }

    boolean start(AppContext context, String blockchainPath, String blockchainRestorePath) {


        WebSocketAppenderAdapter.instance().setElrondWebSocketManager(elrondWebSocketManager);

        BootstrapType bootstrapType = context.getBootstrapType();
        if (bootstrapType.equals(BootstrapType.REBUILD_FROM_DISK)) {
            setupRestoreDir(new File(blockchainRestorePath), new File(blockchainPath));
        }

        ElrondFacade facade = getFacade();
        application = facade.start(context);
        return application != null;
    }

    boolean stop() {
        ElrondFacade facade = getFacade();
        return facade.stop(application);
    }

    BigInteger getBalance(AccountAddress address) {
        ElrondFacade facade = getFacade();
        return facade.getBalance(address, application);
    }

    Transaction send(AccountAddress receiver, BigInteger value) {
        return getFacade().send(receiver, value, application);
    }

    Receipt getReceipt(String transactionHash) {
        return getFacade().getReceipt(transactionHash, application);
    }

    PingResponse ping(String ipAddress, int port) {
        ElrondFacade facade = getFacade();
        return facade.ping(ipAddress, port);
    }

    PKSKPair generatePublicKeyAndPrivateKey() {
        ElrondFacade facade = getFacade();
        return facade.generatePublicKeyAndPrivateKey();
    }

    PKSKPair generatePublicKeyFromPrivateKey(String privateKey) {
        ElrondFacade facade = getFacade();
        return facade.generatePublicKeyFromPrivateKey(privateKey);
    }

    private void setupRestoreDir(File sourceDir, File destinationDir) {
        if (!sourceDir.getAbsolutePath().equals(destinationDir.getAbsolutePath())) {
            deleteDirectory(destinationDir);
            copyDirectory(sourceDir, destinationDir);
        }
    }

    private void copyDirectory(File src, File dest) {
        try {
            FileUtils.copyDirectory(src, dest);
        } catch (IOException ex) {
            System.out.println("Copy directory exception");
            ex.printStackTrace();
        }
    }

    private void deleteDirectory(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException ex) {
            System.out.println("Delete directory exception");
            ex.printStackTrace();
        }
    }

}