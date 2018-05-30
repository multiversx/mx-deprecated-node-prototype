package network.elrond.p2p;

import java.util.ArrayList;
import java.util.List;

public class P2PBroadcastChanel {

    private P2PChannelName name;
    private P2PConnection connection;
    private List<P2PChannelListener> listeners = new ArrayList<>();

    public P2PBroadcastChanel(P2PChannelName chanelName, P2PConnection connection) {
        this.name = chanelName;
        this.connection = connection;
    }

    public P2PChannelName getName() {
        return name;
    }

    public void setName(P2PChannelName name) {
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

