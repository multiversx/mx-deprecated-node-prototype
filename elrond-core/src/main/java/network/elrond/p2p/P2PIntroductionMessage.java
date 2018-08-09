package network.elrond.p2p;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.net.InetAddress;

public class P2PIntroductionMessage implements Serializable {
    private Integer shardId;
    private Number160 peerId;
    private InetAddress inetAddress;
    private int tcpPort;
    private int udpPort;

    public P2PIntroductionMessage(PeerAddress peerAddress, Integer shardId) {
        this.shardId = shardId;
        this.inetAddress = peerAddress.inetAddress();
        this.peerId = peerAddress.peerId();
        this.tcpPort = peerAddress.tcpPort();
        this.udpPort = peerAddress.udpPort();
    }

    public Integer getShardId() {
        return shardId;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public Number160 getPeerId() {
        return peerId;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

}
