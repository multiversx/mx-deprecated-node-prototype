package network.elrond.application;


import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.NTPClient;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.p2p.P2PConnection;

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

    public P2PBroadcastChanel getChanel(P2PChannelName channelName) {
        Util.check(channelName != null, "channelName!=null");
        return channels.get(channelName);
    }

    public void addChanel(P2PBroadcastChanel broadcastChanel) {
        Util.check(broadcastChanel != null, "broadcastChanel!=null");
        this.channels.put(broadcastChanel.getName(), broadcastChanel);
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        Util.check(connection != null, "connection!=null");
        this.connection = connection;
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public void setStillRunning(boolean stillRunning) {
        this.stillRunning = stillRunning;
    }

    public void setBlockchain(Blockchain blockchain) {
        Util.check(blockchain != null, "blockchain!=null");
        this.blockchain = blockchain;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public void setAccounts(Accounts accounts) {
        Util.check(accounts != null, "accounts!=null");
        this.accounts = accounts;
    }


    public void shutdown() {
        this.blockchain.stopPersistenceUnit();
        this.accounts.stopPersistenceUnit();
    }

    public void setPrivateKey(PrivateKey privateKey) {
        Util.check(privateKey != null, "privateKey!=null");
        this.privateKey = privateKey;
        this.publicKey = new PublicKey(privateKey);
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