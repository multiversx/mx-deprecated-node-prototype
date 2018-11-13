package network.elrond.p2p.model;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;

public class P2PIntroductionMessage implements Serializable {
    private Integer shardId;
    PeerAddress peerAddress;

    public P2PIntroductionMessage(PeerAddress peerAddress, Integer shardId) {
        this.shardId = shardId;
        this.peerAddress = peerAddress;
    }

    public Integer getShardId() {
        return shardId;
    }

    public PeerAddress getPeerAddress() {
        return peerAddress;
    }
}
