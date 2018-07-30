package network.elrond.p2p;

import java.util.ArrayList;
import java.util.List;

public class P2PBroadcastChannel {

    private P2PBroadcastChannelName name;
    private P2PConnection connection;
    private List<P2PChannelListener> listeners = new ArrayList<>();

    public P2PBroadcastChannel(P2PBroadcastChannelName chanelName, P2PConnection connection) {
        this.name = chanelName;
        this.connection = connection;
    }

    public P2PBroadcastChannelName getName() {
        return name;
    }

    public void setName(P2PBroadcastChannelName name) {
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

    public String getChannelIdentifier(Integer destinationShard) {
        String indent = name.toString();
        Integer shardIndex = connection.getShard().getIndex();

        if (P2PChannelType.SHARD_LEVEL.equals(name.getType())) {
            indent += connection.getShard().getIndex();
        } else if (P2PChannelType.GLOBAL_LEVEL.equals(name.getType())) {
            if (destinationShard < shardIndex) {
                indent += destinationShard + "" + connection.getShard().getIndex();
            } else if (destinationShard > shardIndex) {
                indent += connection.getShard().getIndex() + "" + destinationShard;
            }
        }

        return indent;
    }

    @Override
    public String toString() {
        return String.format("P2PBroadcastChannel{name=%s, listeners.size()=%d}", name, listeners.size());
    }
}

