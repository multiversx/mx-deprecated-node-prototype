package network.elrond.p2p;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;

public class P2PBroadcastConnection {

    private int peerId;
    private Peer peer;
    private PeerDHT dht;

    public P2PBroadcastConnection(Integer peerId, Peer peer, PeerDHT dht) {
        this.peerId = peerId;
        this.peer = peer;
        this.dht = dht;
    }


    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public PeerDHT getDht() {
        return dht;
    }

    public void setDht(PeerDHT dht) {
        this.dht = dht;
    }


}
