package network.elrond.p2p;

import net.tomp2p.peers.PeerAddress;
import network.elrond.sharding.Shard;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;


public class P2PRequestChannel {

    private P2PRequestChannelName name;
    private P2PConnection connection;
    private P2PRequestObjectHandler<?> handler;
    private HashSet<PeerAddress> peerAddresses;
    private Object peerLock = new Object();


    public P2PRequestChannel(P2PRequestChannelName name, P2PConnection connection) {
        this.name = name;
        this.connection = connection;
        peerAddresses = new HashSet<>();
    }

    public void addPeerAddresses(HashSet<PeerAddress> peerAddresses) {
        synchronized (peerLock) {
            this.peerAddresses.addAll(peerAddresses.stream().filter(Objects::nonNull).collect(Collectors.toList()));
            this.peerAddresses = new HashSet<>(this.peerAddresses.stream().distinct().sorted().collect(Collectors.toSet()));
        }
    }

    public HashSet<PeerAddress> getPeerAddresses() {
        return peerAddresses;
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

