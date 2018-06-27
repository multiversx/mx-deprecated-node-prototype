package network.elrond.application;

import network.elrond.account.Accounts;
import network.elrond.benchmark.StatisticsManager;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.NTPClient;
import network.elrond.consensus.ConsensusState;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.p2p.*;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class AppState implements Serializable {

    private boolean stillRunning = true;
    private Accounts accounts;
    private Blockchain blockchain;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private P2PConnection connection;
    private NTPClient ntpClient;

    private Shard shard;
    private Map<P2PChannelName, P2PBroadcastChanel> broadcastChannels = new HashMap<>();
    private Map<P2PRequestChannelName, P2PRequestChannel> requestChannels = new HashMap<>();

    protected final ArrayBlockingQueue<String> transactionsPool = new ArrayBlockingQueue<>(50000, true);
    private ConsensusState consensusState = new ConsensusState();

    private static final Logger logger = LogManager.getLogger(AppState.class);

    public final Object lockerSyncPropose = new Object();

    private StatisticsManager statisticsManager = new StatisticsManager(System.currentTimeMillis());

    public P2PRequestChannel getChanel(P2PRequestChannelName channelName) {
        logger.traceEntry("params: {}", channelName);
        Util.check(channelName != null, "channelName!=null");
        return logger.traceExit(requestChannels.get(channelName));
    }

    public void addChanel(P2PRequestChannel requestChanel) {
        logger.traceEntry("params: {}", requestChanel);
        Util.check(requestChanel != null, "requestChanel!=null");
        this.requestChannels.put(requestChanel.getName(), requestChanel);
        logger.traceExit();
    }

    public P2PBroadcastChanel getChanel(P2PChannelName channelName) {
        logger.traceEntry("params: {}", channelName);
        Util.check(channelName != null, "channelName!=null");
        return logger.traceExit(broadcastChannels.get(channelName));
    }

    public void addChanel(P2PBroadcastChanel broadcastChanel) {
        logger.traceEntry("params: {}", broadcastChanel);
        Util.check(broadcastChanel != null, "broadcastChanel!=null");
        this.broadcastChannels.put(broadcastChanel.getName(), broadcastChanel);
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

    public NTPClient getNtpClient() {
        return ntpClient;
    }

    public void setNtpClient(NTPClient ntpClient) {
        this.ntpClient = ntpClient;
    }

    public ConsensusState getConsensusState() {
        return consensusState;
    }

    public Shard getShard() {
        return shard;
    }

    public void setShard(Shard shard) {
        this.shard = shard;
    }

    public ArrayBlockingQueue<String> getTransactionPool(){
        return (transactionsPool);
    }

    public void addTransactionToPool(String hash){
        transactionsPool.add(hash);
    }

    public StatisticsManager getStatisticsManager(){
        return statisticsManager;
    }
}