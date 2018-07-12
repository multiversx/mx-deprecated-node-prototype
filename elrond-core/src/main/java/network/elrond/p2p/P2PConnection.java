package network.elrond.p2p;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import network.elrond.core.ObjectUtil;
import network.elrond.core.Util;
import network.elrond.sharding.Shard;

import java.util.ArrayList;
import java.util.List;

public class P2PConnection {

    private String nodeName;
    private Peer peer;
    private PeerDHT dht;
    private Shard shard;

    private ObjectDataReply dataReplyCallback;
    private List<P2PBroadcastChanel> broadcastChannels = new ArrayList<>();
    private List<P2PRequestChannel> requestChannels = new ArrayList<>();

    public P2PConnection(String nodeName, Peer peer, PeerDHT dht) {
        this.nodeName = nodeName;
        this.peer = peer;
        this.dht = dht;

        this.dataReplyCallback = (sender, request) -> {

            if (request instanceof P2PBroadcastMessage) {
                return handleBroadcast(sender, (P2PBroadcastMessage) request);
            }

            if (request instanceof P2PRequestMessage) {
                return handleRequest(sender, (P2PRequestMessage) request);
            }


            throw new RuntimeException("Not supported request" + request);
        };
    }

    private Object handleRequest(PeerAddress sender, P2PRequestMessage request) throws InterruptedException {

        for (P2PRequestChannel chanel : requestChannels) {

            if (!ObjectUtil.isEqual(request.getChannelName(), chanel.getName())) {
                continue;
            }

            P2PRequestObjectHandler handler = chanel.getHandler();
            return handler.get(request);

        }

        return null;
    }

    private Object handleBroadcast(PeerAddress sender, P2PBroadcastMessage request) throws InterruptedException {

        for (P2PBroadcastChanel chanel : broadcastChannels) {
            for (P2PChannelListener listener : chanel.getListeners()) {

                // Filter response for channel
                P2PBroadcastMessage message = request;
                if (!message.isForChannel(chanel.getName())) {
                    continue;
                }

                listener.onReceiveMessage(sender, request);
            }
        }

        return null;
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

    public Shard getShard() {
        return shard;
    }

    public void setShard(Shard shard) {
        this.shard = shard;
    }

    public ObjectDataReply registerChannel(P2PBroadcastChanel channel) {
        broadcastChannels.add(channel);
        return dataReplyCallback;
    }

    public ObjectDataReply registerChannel(P2PRequestChannel channel) {
        requestChannels.add(channel);
        return dataReplyCallback;
    }

    public P2PRequestChannel getRequestChannel(String channelName) {
        Util.check(channelName != null, "channelName != null");

        for (P2PRequestChannel requestChannel : requestChannels) {
            P2PRequestChannelName requestChannelName = requestChannel.getName();
            if (channelName.equals(requestChannelName.getName())) {
                return requestChannel;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return (String.format("P2PConnection{MEM=%s, nodeName=%s, shard=%s}", (Object) dht, nodeName, shard));
    }
}
