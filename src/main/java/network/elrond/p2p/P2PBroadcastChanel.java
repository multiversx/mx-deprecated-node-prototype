package network.elrond.p2p;

import java.util.ArrayList;
import java.util.List;

public class P2PBroadcastChanel {

    private String name;
    private P2PConnection connection;
    private List<P2PChannelListener> listeners = new ArrayList<>();

    public P2PBroadcastChanel(String chanelName, P2PConnection connection) {
        this.name = chanelName;
        this.connection = connection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        this.connection = connection;
    }

    public List<P2PChannelListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<P2PChannelListener> listeners) {
        this.listeners = listeners;
    }
}

