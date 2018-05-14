package network.elrond.application;


import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PBroadcastConnection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppState implements Serializable {

    private boolean stillRunning = true;

    private P2PBroadcastConnection connection;
    private Map<String, P2PBroadcastChanel> channels = new HashMap<>();

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