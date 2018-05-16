package network.elrond.application;


import network.elrond.data.Block;
import network.elrond.data.SyncData;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PBroadcastConnection;

import java.awt.font.TransformAttribute;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppState implements Serializable {

    private boolean stillRunning = true;

    private P2PBroadcastConnection connection;
    private Map<String, P2PBroadcastChanel> channels = new HashMap<>();

    public SyncData<Transaction> syncDataTx = new SyncData<>();
    public SyncData<Block> syncDataBlk = new SyncData<>();

    public P2PBroadcastChanel getChanel(String name) {
        return channels.get(name);
    }

    public void addChanel(String name, P2PBroadcastChanel chanel) {
        this.channels.put(name, chanel);
    }

    public P2PBroadcastConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PBroadcastConnection connection) {
        this.connection = connection;
    }

    public boolean isStillRunning() {
        return stillRunning;
    }

    public void setStillRunning(boolean stillRunning) {
        this.stillRunning = stillRunning;
    }
}