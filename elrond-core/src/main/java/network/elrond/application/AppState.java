package network.elrond.application;

import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.NTPClient;
import network.elrond.consensus.ConsensusStateHolder;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
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

    private ConsensusStateHolder consensusStateHolder = new ConsensusStateHolder();

    private static final Logger logger = LogManager.getLogger(AppState.class);

    public P2PBroadcastChanel getChanel(P2PChannelName channelName) {
        logger.traceEntry("params: {}", channelName);
        Util.check(channelName != null, "channelName!=null");
        return logger.traceExit(channels.get(channelName));
    }

    public void addChanel(P2PBroadcastChanel broadcastChanel) {
        logger.traceEntry("params: {}", broadcastChanel);
        Util.check(broadcastChanel != null, "broadcastChanel!=null");
        this.channels.put(broadcastChanel.getName(), broadcastChanel);
        logger.traceExit();
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        logger.traceEntry("params: {}", connection);
        Util.check(connection != null, "connection!=null");
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
        Util.check(blockchain != null, "blockchain!=null");
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
        Util.check(accounts != null, "accounts!=null");
        this.accounts = accounts;
        logger.traceExit();
    }

    public void shutdown() {
        logger.traceEntry();
        this.blockchain.stopPersistenceUnit();
        this.accounts.stopPersistenceUnit();
        logger.traceExit();
    }

    public void setPrivateKey(PrivateKey privateKey) {
        logger.traceEntry("params: {}", privateKey);
        Util.check(privateKey != null, "privateKey!=null");
        this.privateKey = privateKey;
        this.publicKey = new PublicKey(privateKey);
        logger.traceExit();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }


    public synchronized boolean isLock() {
        return lock;
    }

    public NTPClient getNtpClient() {
        return (ntpClient);
    }

    public void setNtpClient(NTPClient ntpClient) {
        this.ntpClient = ntpClient;
    }

    public ConsensusStateHolder getConsensusStateHolder() {
        return (consensusStateHolder);
    }

    public synchronized void acquireLock() {
        while (lock) {
            ThreadUtil.sleep(50);
        }

        lock = true;
    }

    public synchronized void releaseLock() {
        lock = false;
    }
}