package network.elrond.application;


import network.elrond.account.AccountStateServiceImpl;
import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.NTPClient;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.p2p.P2PConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppState implements Serializable {

    private boolean stillRunning = true;

    private boolean lock = false;

    private Accounts accounts;
    private Blockchain blockchain;


    private PublicKey publicKey;
    private PrivateKey privateKey;

    private P2PConnection connection;
    private Map<P2PChannelName, P2PBroadcastChanel> channels = new HashMap<>();

    private NTPClient ntpClient = null;

    private static final Logger logger = LogManager.getLogger(AppState.class);

    public P2PBroadcastChanel getChanel(P2PChannelName name) {
        logger.traceEntry("params: {}", name);
        if (name == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Name cannot be null");
            logger.throwing(ex);
            throw ex;
        }

        return logger.traceExit(channels.get(name));
    }

    public void addChanel(P2PChannelName name, P2PBroadcastChanel channel) {
        logger.traceEntry("params: {} {}", name, channel);
        if (name == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Name cannot be null");
            logger.throwing(ex);
            throw ex;
        }

        if (channel == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Chanel cannot be null");
            logger.throwing(ex);
            throw ex;
        }

        this.channels.put(name, channel);

        logger.traceExit();
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        logger.traceEntry("params: {}", connection);

        if (connection == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Connection cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.connection = connection;
        logger.traceExit();
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public void setStillRunning(boolean stillRunning) {
        this.stillRunning = stillRunning;
    }

    public void setBlockchain(Blockchain blockchain) {
        logger.traceEntry("params: {}", blockchain);
        if (blockchain == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Blockchain cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.blockchain = blockchain;
        logger.traceExit();
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public void setAccounts(Accounts accounts) {
        logger.traceEntry("params: {}", accounts);
        if (accounts == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Accounts cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.accounts = accounts;
        logger.traceExit();
    }

    public void shutdown() {
        this.blockchain.stopPersistenceUnit();
        this.accounts.stopPersistenceUnit();
    }

    public void setPrivateKey(PrivateKey privateKey) {
        logger.traceEntry("params: {}", privateKey);
        if (privateKey == null) {
            IllegalArgumentException ex = new IllegalArgumentException("PrivateKey cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.privateKey = privateKey;
        logger.traceExit();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        logger.traceEntry("params: {}", publicKey);
        if (publicKey == null) {
            IllegalArgumentException ex = new IllegalArgumentException("PublicKey cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.publicKey = publicKey;
        logger.traceExit();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }


    public synchronized boolean isLock() {
        return lock;
    }

    public synchronized void setLock() {
        this.lock = true;
    }

    public synchronized void clearLock() {
        this.lock = false;
    }

    public NTPClient getNtpClient(){
        return(ntpClient);
    }

    public void setNtpClient(NTPClient ntpClient){
        this.ntpClient = ntpClient;
    }

}