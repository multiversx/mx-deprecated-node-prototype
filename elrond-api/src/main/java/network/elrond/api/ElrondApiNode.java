package network.elrond.api;

import network.elrond.Application;
import network.elrond.ElrondFacade;
import network.elrond.ElrondFacadeImpl;
import network.elrond.account.AccountAddress;
import network.elrond.api.log.WebSocketAppenderAdapter;
import network.elrond.api.manager.ElrondWebSocketManager;
import network.elrond.application.AppContext;
import network.elrond.blockchain.Blockchain;
import network.elrond.core.ResponseObject;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.BootstrapType;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private ResponseObject cachedBenchMarkResultResponse = null;

    private Thread thrBenchmarkResultSolver = new Thread(() -> {

        while (true){
            ThreadUtil.sleep(2000);

            if (application == null){
                continue;
            }

            if (application.getState() == null){
                continue;
            }

            if (!application.getState().isStillRunning()){
                return;
            }

            ElrondFacade facade = getFacade();
            cachedBenchMarkResultResponse = facade.getBenchmarkResult("", application);

            logger.debug("Got cachedBenchmarkResultResponse: {}", cachedBenchMarkResultResponse);
        }
    });

    public Application getApplication() {
        return application;
    }

    private ElrondFacade getFacade() {
        return new ElrondFacadeImpl();
    }

    boolean start(AppContext context, String blockchainPath, String blockchainRestorePath) throws IOException {
        logger.traceEntry("params: {} {} {}", context, blockchainPath, blockchainRestorePath);

        WebSocketAppenderAdapter.instance().setElrondWebSocketManager(elrondWebSocketManager);

        BootstrapType bootstrapType = context.getBootstrapType();
        if (bootstrapType.equals(BootstrapType.REBUILD_FROM_DISK)) {
            logger.trace("REBUILD_FROM_DISK selected");
            setupRestoreDir(new File(blockchainRestorePath), new File(blockchainPath));
        }

        if (bootstrapType.equals(BootstrapType.START_FROM_SCRATCH)) {
            logger.trace("START_FROM_SCRATCH selected");
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

    ResponseObject getBalance(AccountAddress address) {
        logger.traceEntry("params: {}", address);
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.getBalance(address, application));
    }

    ResponseObject getBenchmarkResult() {
        if (thrBenchmarkResultSolver.getState() == Thread.State.NEW){
            //thread is not started

            ElrondFacade facade = getFacade();
            cachedBenchMarkResultResponse = facade.getBenchmarkResult("", application);

            thrBenchmarkResultSolver.start();

            return cachedBenchMarkResultResponse;
        }

        return cachedBenchMarkResultResponse;
    }

    ResponseObject sendMultipleTransactions(AccountAddress receiver, BigInteger value, Integer nrTransactions) {
        logger.traceEntry("params: {} {}", receiver, value);
        return logger.traceExit(getFacade().sendMultipleTransactions(receiver, value, nrTransactions, application));
    }

    ResponseObject sendMultipleTransactionsToAllShards(BigInteger value, Integer nrTransactions) {
        logger.traceEntry("params: {} {}", value, nrTransactions);
        return logger.traceExit(getFacade().sendMultipleTransactionsToAllShards(value, nrTransactions, application));
    }

    ResponseObject send(AccountAddress receiver, BigInteger value) {
        logger.traceEntry("params: {} {}", receiver, value);
        return logger.traceExit(getFacade().send(receiver, value, application));
    }

    ResponseObject getReceipt(String transactionHash) {
        logger.traceEntry("params: {}", transactionHash);
        return logger.traceExit(getFacade().getReceipt(transactionHash, application));
    }

    ResponseObject ping(String ipAddress, int port) {
        logger.traceEntry("params: {} {}", ipAddress, port);
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.ping(ipAddress, port));
    }

    ResponseObject checkFreePort(String ipAddress, int port) {
        logger.traceEntry("params: {} {}", ipAddress, port);
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.checkFreePort(ipAddress, port));
    }

    ResponseObject generatePublicKeyAndPrivateKey(String privateKey) {
        logger.traceEntry();
        ElrondFacade facade = getFacade();
        return logger.traceExit(facade.generatePublicKeyAndPrivateKey(privateKey));
    }

    ResponseObject getTransactionFromHash(String transactionHash){
        logger.traceEntry("params: {}", transactionHash);
        ElrondFacade facade = getFacade();

        if (application == null){
            logger.warn("Invalid application state, application is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, application is null", null));
        }

        if (application.getState() == null){
            logger.warn("Invalid application state, state is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, state is null", null));
        }

        Blockchain blockchain = application.getState().getBlockchain();

        return logger.traceExit(facade.getTransactionFromHash(transactionHash, blockchain));
    }

    ResponseObject getBlockFromHash(String blockHash){
        logger.traceEntry("params: {}", blockHash);
        ElrondFacade facade = getFacade();

        if (application == null){
            logger.warn("Invalid application state, application is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, application is null", null));
        }

        if (application.getState() == null){
            logger.warn("Invalid application state, state is null");
            return logger.traceExit(new ResponseObject(false, "Invalid application state, state is null", null));
        }

        Blockchain blockchain = application.getState().getBlockchain();

        return logger.traceExit(facade.getBlockFromHash(blockHash, blockchain));
    }

    ResponseObject getPrivatePublicKeyShard(){
        ElrondFacade facade = getFacade();

        return facade.getPrivatePublicKeyShard(application);
    }

    ResponseObject getNextPrivateKey(String requestAddress ){
        logger.traceEntry();
        ElrondFacade facade = getFacade();
        ResponseObject ro = facade.getNextPrivateKey(requestAddress);
        String nextPrivateKey = ro.getPayload().toString();

        SendBalanceToNewNode(nextPrivateKey);

        return logger.traceExit(ro);
    }

    void getLog(){
        logger.traceEntry();

        ElrondFacade facade = getFacade();
    }

    private void SendBalanceToNewNode(String nextPrivateKey) {
        Runnable myrunnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String publicKey = nextPrivateKey.split(";")[2];
                send(AccountAddress.fromHexString(publicKey), BigInteger.valueOf(1000000000));
            }
        };

        new Thread(myrunnable).start();
    }

    private void setupRestoreDir(File sourceDir, File destinationDir) throws IOException {
        logger.traceEntry("params: {} {}", sourceDir, destinationDir);
        if (!sourceDir.getAbsolutePath().equals(destinationDir.getAbsolutePath())) {
            logger.trace("source and destination paths are different");
            Util.deleteDirectory(destinationDir);
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
}