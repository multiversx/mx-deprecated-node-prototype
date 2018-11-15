package network.elrond.p2p.model;

import network.elrond.p2p.P2PRequestObjectHandler;
import network.elrond.sharding.Shard;


public class P2PRequestChannel {

    private final P2PRequestChannelName name;
    private final P2PConnection connection;
    private P2PRequestObjectHandler<?> handler;


    public P2PRequestChannel(P2PRequestChannelName name, P2PConnection connection) {
        this.name = name;
        this.connection = connection;
    }

    public P2PRequestChannelName getName() {
        return name;
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public P2PRequestObjectHandler<?> getHandler() {
        return handler;
    }

    public void setHandler(P2PRequestObjectHandler<?> handler) {
        this.handler = handler;
    }

    @Override
    public String toString() {
        return "P2PRequestChannel{" + "name=" + name + ", connection=" + connection + '}';
    }

    public String getChannelIdentifier(Shard shard) {
        return name.name() + shard.getIndex();
    }
}

