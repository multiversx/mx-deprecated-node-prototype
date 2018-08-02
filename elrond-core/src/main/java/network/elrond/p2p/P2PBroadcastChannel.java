package network.elrond.p2p;

import net.tomp2p.peers.PeerAddress;

import java.util.*;

public class P2PBroadcastChannel {

    private P2PBroadcastChannelName name;
    private P2PConnection connection;
    private List<P2PChannelListener> listeners = new ArrayList<>();
    private Map<String, HashSet<PeerAddress>> peerAddresses;
    private Object peerLock = new Object();

    public P2PBroadcastChannel(P2PBroadcastChannelName chanelName, P2PConnection connection) {
        this.name = chanelName;
        this.connection = connection;
        this.peerAddresses = new HashMap<>();
    }

    public void addPeerAddresses(String channelHash, HashSet<PeerAddress> peerAddresses) {
        synchronized (peerLock) {
            HashSet<PeerAddress> hashSet = this.peerAddresses.get(channelHash);
            if (hashSet == null) {
                hashSet = new HashSet<>();
                this.peerAddresses.put(channelHash, hashSet);
            }
            hashSet.addAll(peerAddresses);
        }
    }

    public HashSet<PeerAddress> getPeerAddresses(String hash) {
        synchronized (peerLock) {
            return new HashSet<>(peerAddresses.get(hash));
        }
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

