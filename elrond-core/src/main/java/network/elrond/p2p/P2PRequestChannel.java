package network.elrond.p2p;

import net.tomp2p.peers.PeerAddress;
import network.elrond.sharding.Shard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class P2PRequestChannel {

    private P2PRequestChannelName name;
    private P2PConnection connection;
    private P2PRequestObjectHandler<?> handler;
    private Map<String, HashSet<PeerAddress>> peerAddresses;
    private Object peerLock = new Object();


    public P2PRequestChannel(P2PRequestChannelName name, P2PConnection connection) {
        this.name = name;
        this.connection = connection;
        peerAddresses = new HashMap<>();
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


    public P2PRequestChannelName getName() {
        return name;
    }

    public void setName(P2PRequestChannelName name) {
        this.name = name;
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        this.connection = connection;
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

