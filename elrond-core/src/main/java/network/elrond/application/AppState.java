package network.elrond.application;


import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.Block;
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

    private AppMode mode = null;

    private Accounts accounts;
    private Blockchain blockchain;
    private Block currentBlock;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private P2PConnection connection;
    private Map<P2PChannelName, P2PBroadcastChanel> channels = new HashMap<>();


    public P2PBroadcastChanel getChanel(P2PChannelName name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        return channels.get(name);
    }

    public void addChanel(P2PChannelName name, P2PBroadcastChanel channel) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Chanel cannot be null");
        }

        this.channels.put(name, channel);
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public void setStillRunning(boolean stillRunning) {
        this.stillRunning = stillRunning;
    }

    public void setBlockchain(Blockchain blockchain) {
        if (blockchain == null) {
            throw new IllegalArgumentException("Blockchain cannot be null");
        }
        this.blockchain = blockchain;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public void setAccounts(Accounts accounts) {
        if (accounts == null) {
            throw new IllegalArgumentException("Accounts cannot be null");
        }
        this.accounts = accounts;
    }

    public AppMode getMode() {
        return mode;
    }

    public void setMode(AppMode mode) {
        this.mode = mode;
    }

    public boolean isAllowed(AppMode mode) {
        return this.mode == null || this.mode.equals(mode);
    }

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(Block currentBlock) {
        if (currentBlock == null) {
            throw new IllegalArgumentException("CurrentBlock cannot be null");
        }
        this.currentBlock = currentBlock;
    }

    public void shutdown() {
        this.blockchain.stopPersistenceUnit();
        this.accounts.stopPersistenceUnit();
    }

    public void setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("PrivateKey cannot be null");
        }
        this.privateKey = privateKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("PublicKey cannot be null");
        }
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}