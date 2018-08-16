package network.elrond.application;

import network.elrond.AsciiTable;
import network.elrond.account.Accounts;
import network.elrond.benchmark.ElrondSystemTimerImpl;
import network.elrond.benchmark.StatisticsManager;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.TransactionsPool;
import network.elrond.chronology.NTPClient;
import network.elrond.consensus.ConsensusData;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.AsciiPrintable;
import network.elrond.p2p.*;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppState implements Serializable, AsciiPrintable {

    private static final Logger logger = LogManager.getLogger(AppState.class);

    private boolean stillRunning = true;
    public final Object lockerSyncPropose = new Object();

    private Shard shard;
    private Accounts accounts;
    private Blockchain blockchain;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private NTPClient ntpClient;
    private P2PConnection connection;

    private Map<P2PBroadcastChannelName, P2PBroadcastChannel> broadcastChannels = new HashMap<>();
    private Map<P2PRequestChannelName, P2PRequestChannel> requestChannels = new HashMap<>();

    private ConsensusData consensusData = new ConsensusData();

    private StatisticsManager statisticsManagers;

    public P2PRequestChannel getChannel(P2PRequestChannelName channelName) {
        logger.traceEntry("params: {}", channelName);
        Util.check(channelName != null, "channelName!=null");
        return logger.traceExit(requestChannels.get(channelName));
    }

    public void addChannel(P2PRequestChannel requestChanel) {
        logger.traceEntry("params: {}", requestChanel);
        Util.check(requestChanel != null, "requestChanel!=null");
        this.requestChannels.put(requestChanel.getName(), requestChanel);
        logger.traceExit();
    }

    public P2PBroadcastChannel getChannel(P2PBroadcastChannelName channelName) {
        logger.traceEntry("params: {}", channelName);
        Util.check(channelName != null, "channelName!=null");
        return logger.traceExit(broadcastChannels.get(channelName));
    }

    public void addChannel(P2PBroadcastChannel broadcastChannel) {
        logger.traceEntry("params: {}", broadcastChannel);
        Util.check(broadcastChannel != null, "broadcastChanel!=null");
        this.broadcastChannels.put(broadcastChannel.getName(), broadcastChannel);
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

    public ConsensusData getConsensusData() {
        return consensusData;
    }

    public Shard getShard() {
        return shard;
    }

    public void setShard(Shard shard) {
        this.shard = shard;
    }

    public StatisticsManager getStatisticsManager() {
        logger.warn("Transactions in Pool: {}", getPool().getTransactions().size());
        if (statisticsManagers == null) {
            statisticsManagers = new StatisticsManager(new ElrondSystemTimerImpl(), getShard().getIndex());
        }
        return statisticsManagers;
    }

    public TransactionsPool getPool() {
        return blockchain.getPool();
    }

    @Override
    public AsciiTable print() {

        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(90);

        table.getColumns().add(new AsciiTable.Column("AppState "));
        table.getColumns().add(new AsciiTable.Column(Util.byteArrayToHexString(getPublicKey().getValue())));

        AsciiTable.Row row0 = new AsciiTable.Row();
        row0.getValues().add("Shard");
        row0.getValues().add(getShard().getIndex().toString());
        table.getData().add(row0);

        AsciiTable.Row row1 = new AsciiTable.Row();
        row1.getValues().add("Pool");
        row1.getValues().add(String.valueOf(getPool().getTransactions().size()));
        table.getData().add(row1);

        table.calculateColumnWidth();
        return table;
    }
}