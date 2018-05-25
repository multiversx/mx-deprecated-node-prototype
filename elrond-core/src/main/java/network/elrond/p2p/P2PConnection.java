package network.elrond.p2p;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.rpc.ObjectDataReply;

import java.util.ArrayList;
import java.util.List;

public class P2PConnection {

    private int peerId;
    private Peer peer;
    private PeerDHT dht;

    private ObjectDataReply dataReplyCallback;
    private List<P2PBroadcastChanel> channels = new ArrayList<>();

    public P2PConnection(Integer peerId, Peer peer, PeerDHT dht) {
        this.peerId = peerId;
        this.peer = peer;
        this.dht = dht;

        this.dataReplyCallback = (sender, request) -> {

            for (P2PBroadcastChanel chanel : channels) {
                for (P2PChannelListener listener : chanel.getListeners()) {

                    // Filter response for channel
                    P2PBroadcastMessage message = (P2PBroadcastMessage) request;
                    if (!message.isForChannel(chanel.getName())) {
                        continue;
                    }

                    listener.onReciveMessage(sender, (P2PBroadcastMessage) request);
                }
            }
            return null;
        };
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

    public ObjectDataReply registerChannel(P2PBroadcastChanel channel) {
        channels.add(channel);
        return dataReplyCallback;
    }
}
