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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.Fun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

@Component
class ElrondApiNode {
    private static final Logger logger = LogManager.getLogger(ElrondApiNode.class);

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
        logger.traceEntry("params: {} {} {}", context, blockchainPath, blockchainRestorePath);

        WebSocketAppenderAdapter.instance().setElrondWebSocketManager(elrondWebSocketManager);

        BootstrapType bootstrapType = context.getBootstrapType();
        if (bootstrapType.equals(BootstrapType.REBUILD_FROM_DISK)) {
            logger.trace("REBUILD_FROM_DISK selected!");
            setupRestoreDir(new File(blockchainRestorePath), new File(blockchainPath));
        }

        if (bootstrapType.equals(BootstrapType.START_FROM_SCRATCH)){
            logger.trace("START_FROM_SCRATCH selected!");
        }

        logger.trace("Starting facade...");
        ElrondFacade facade = getFacade();
        application = facade.start(context);
        return logger.traceExit(application != null);
    }

    boolean stop() {
        logger.traceEntry();
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.stop(application));
    }

    BigInteger getBalance(AccountAddress address) {
        logger.traceEntry("params: {}", address);
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.getBalance(address, application));
    }

    Transaction send(AccountAddress receiver, BigInteger value) {
        logger.traceEntry("params: {} {}", receiver, value);
        return logger.traceExit(getFacade().send(receiver, value, application));
    }

    Receipt getReceipt(String transactionHash) {
        logger.traceEntry("params: {}", transactionHash);
        return logger.traceExit(getFacade().getReceipt(transactionHash, application));
    }

    PingResponse ping(String ipAddress, int port) {
        logger.traceEntry("params: {} {}", ipAddress, port);
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.ping(ipAddress, port));
    }

    PKSKPair generatePublicKeyAndPrivateKey(String privateKey) {
        logger.traceEntry();
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.generatePublicKeyAndPrivateKey(privateKey));
    }

    private void setupRestoreDir(File sourceDir, File destinationDir) {
        logger.traceEntry("params: {} {}", sourceDir, destinationDir);
        if (!sourceDir.getAbsolutePath().equals(destinationDir.getAbsolutePath())) {
            logger.trace("source and destination paths are different!");
            deleteDirectory(destinationDir);
            copyDirectory(sourceDir, destinationDir);
        }
        logger.traceExit();
    }

    private void copyDirectory(File src, File dest) {
        logger.traceEntry("params: {} {}", src, dest);
        try {
            FileUtils.copyDirectory(src, dest);
            logger.trace("done");
        } catch (IOException ex) {
            logger.throwing(ex);
        }
        logger.traceExit();
    }

    private void deleteDirectory(File dir) {
        logger.traceEntry("params: {}", dir);
        try {
            FileUtils.deleteDirectory(dir);
            logger.trace("done");
        } catch (IOException ex) {
            logger.throwing(ex);
        }
        logger.traceExit();
    }

}