package network.elrond.p2p;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import network.elrond.core.ObjectUtil;
import network.elrond.core.Util;
import network.elrond.p2p.handlers.BroadcastStructuredHandler;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class P2PConnection {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(P2PConnection.class);
    private String nodeName;
    private Peer peer;
    private PeerDHT dht;
    private Shard shard;
    BroadcastStructuredHandler broadcastHandler;

    private ObjectDataReply dataReplyCallback;
    private List<P2PBroadcastChannel> broadcastChannels = new ArrayList<>();
    private List<P2PRequestChannel> requestChannels = new ArrayList<>();

    // Buckets for each shard containing connected peers
    private Map<Integer, HashSet<PeerAddress>> bucketsPeers = new ConcurrentHashMap<>();

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

            if (request instanceof P2PReplyIntroductionMessage) {
                return handleReplyIntroduction(sender, (P2PReplyIntroductionMessage) request);
            }


            throw new RuntimeException("Not supported request" + request);
        };
    }

    public BroadcastStructuredHandler getBroadcastHandler() {
        return broadcastHandler;
    }

    public void setBroadcastHandler(BroadcastStructuredHandler broadcastHandler) {
        this.broadcastHandler = broadcastHandler;
    }

    public HashSet<PeerAddress> getPeersOnShard(Integer shardId) {
        HashSet<PeerAddress> result = bucketsPeers.get(shardId);
        return result == null ? new HashSet<>() : new HashSet<>(result);
    }

    public HashSet<PeerAddress> getPeersOnAllShards() {
        HashSet<PeerAddress> result = new HashSet<>();

        for (int shardId : bucketsPeers.keySet()){
            HashSet<PeerAddress> peers = bucketsPeers.get(shardId);

            if (peers == null){
                continue;
            }

            result.addAll(peers);
        }

        return result;
    }

    public synchronized void addPeerOnShard(PeerAddress peerAddress, Integer shardId) {
        HashSet<PeerAddress> peersOnShard = getPeersOnShard(shardId);
        peersOnShard.add(peerAddress);
        bucketsPeers.put(shardId, peersOnShard);
    }

    public Map<Integer, HashSet<PeerAddress>> getAllPeers() {
        return new HashMap<>(bucketsPeers);
    }


    private Object handleRequest(PeerAddress sender, P2PRequestMessage request) {

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

        for (P2PBroadcastChannel channel : broadcastChannels) {
            for (P2PChannelListener listener : channel.getListeners()) {

                // Filter response for channel
                if (!request.isForChannel(channel.getName())) {
                    continue;
                }

                listener.onReceiveMessage(sender, request);
            }
        }

        return null;
    }

    private Object handleReplyIntroduction(PeerAddress sender, P2PReplyIntroductionMessage request) {
        logger.fatal("handleReplyIntroduction...");

        if (request == null) {
            return null;
        }

        for (P2PIntroductionMessage msg : request.getBucketList()) {
            PeerAddress peerAddress = msg.getPeerAddress();
            addPeerOnShard(peerAddress, msg.getShardId());

        }

        logger.fatal("new bucket list {}", getAllPeers());

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

    public ObjectDataReply registerChannel(P2PBroadcastChannel channel) {
        broadcastChannels.add(channel);
        return dataReplyCallback;
    }

    public ObjectDataReply registerChannel(P2PRequestChannel channel) {
        requestChannels.add(channel);
        return dataReplyCallback;
    }

    public ObjectDataReply getDataReplyCallback() {
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
        return (String.format("P2PConnection{MEM=%s, nodeName=%s, shard=%s}", dht, nodeName, shard));
    }
}
