package network.elrond.p2p.handlers;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureChannelCreator;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.message.Message;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.StructuredBroadcastHandler;
import net.tomp2p.p2p.builder.BroadcastBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMap;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.ConcurrentCacheMap;
import net.tomp2p.utils.Utils;
import network.elrond.application.AppState;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.NavigableMap;

public class BroadcastStructuredHandler extends StructuredBroadcastHandler {
    private final ConcurrentCacheMap<Number160, Boolean> cache = new ConcurrentCacheMap<Number160, Boolean>();
    private volatile Peer peer;
    private volatile AppState appState;

    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(BroadcastStructuredHandler.class);

    public BroadcastStructuredHandler() {
        this.peer = null;
        this.appState = null;
    }

    public AppState getAppState(){
        return appState;
    }

    public void setAppState(AppState appState){
        this.appState = appState;
    }

    @Override
    public StructuredBroadcastHandler receive(Message message) {
        //sanity checks
        if (peer == null){
            return this;
        }

        if (message == null){
            return this;
        }

        if (appState == null){
            return this;
        }

        final PeerAddress sender = message.sender();

        final Number160 messageKey = message.key(0);
        //filter out same message processed
        if (twiceSeen(messageKey)){
            //logger.debug("{} already received the message: {}", peer.peerAddress().tcpPort(), messageKey);
            return this;
        }

        final NavigableMap<Number640, Data> dataMap;
        if (message.dataMap(0) != null) {
            dataMap = message.dataMap(0).dataMap();
        } else {
            dataMap = null;
        }

        //##############################################################DO THE MAGIC HERE !!!!!!!!!


        //OLD JLS BELOW
        //we got the broadcast message, we shall interpret it
        //first, add the new PeerAddress in the PeerAddress map corresponding to the shard
        //then, direct message to the peer to advertise itself
//        PeerAddressShard peerAddressShard = null;
//
//        try {
//            peerAddressShard = (PeerAddressShard) dataMap.values().iterator().next().object();
//        } catch (Exception ex){
//            logger.catching(ex);
//        }
//
//        if (peerAddressShard == null){
//            return this;
//        }
//
//        appState.addPeerAddressShard(peerAddressShard);
//
//        PeerAddress peerAddressReceiver = null;
//
//        try {
//            peerAddressReceiver = new PeerAddress(peerAddressShard.getId(), peerAddressShard.getAddress(),
//                    peerAddressShard.getPort(), peerAddressShard.getPort());
//        } catch (Exception ex){
//            logger.catching(ex);
//        }
//
//        if (peerAddressReceiver == null){
//            return this;
//        }
//
//        PeerAddressShard peerAddressShardSender = null;
//        peerAddressShardSender.setAddress(peer.peerAddress().inetAddress());
//        peerAddressShardSender.setPort(peer.peerAddress().tcpPort());
//        peerAddressShardSender.setId(peer.peerID());
//        peerAddressShardSender.setShard(appState.getShard().getIndex());
//
//
//        NavigableMap<Number640, Data> dataMapSend = new TreeMap<>();
//        try {
//            dataMapSend.put(Number640.ZERO, new Data(peerAddressShardSender));
//        } catch (Exception ex){
//            logger.catching(ex);
//            return null;
//        }
//
//        //the message key *must* be unique for broadcast you do. In order to avoid duplicates, multiple
//        //messages with the same message key will be ignored, thus, subsequent broadcast may fail.
//        Number160 messageKey = Number160.createHash("blub");
//        //take any peer and send broadcast
//        peers.get(19).broadcast(messageKey).dataMap(dataMap).start();
//
//        peer.sendDirect(
//
//        //logger.info("{} received the message: {}", peer.peerAddress().tcpPort(), messageKey);

        //##############################################################END DO THE MAGIC HERE !!!!!!!!!

        //broadcast
        final int hopCount = message.intAt(0);
        //get all verified peers
        List<PeerAddress> listToSend = peer.peerBean().peerMap().all();
        //to be checked if we send to the verified peers or all known peers (even overflowed peers)
        //listToSend.addAll(peer.peerBean().peerMap().allOverflow());

        for (PeerAddress peerAddress : listToSend){
            if (peerAddress == sender){
                //not returning to sender
                continue;
            }

            int bucketNr = PeerMap.classMember(peerAddress.peerId(),
                    peer.peerID());
            //magic send
            doSend(messageKey, dataMap, hopCount, message.isUdp(), peerAddress,
                    bucketNr);
        }

        return this;
    }

    @Override
    public StructuredBroadcastHandler init(Peer peer) {
        this.peer = peer;
        return super.init(peer);
    }

    private boolean twiceSeen(final Number160 messageKey) {
        Boolean isInCache = cache.putIfAbsent(messageKey, Boolean.TRUE);
        if (isInCache != null) {
            // ttl refresh
            cache.put(messageKey, Boolean.TRUE);
            return true;
        }
        return false;
    }

    private void doSend(final Number160 messageKey,
                        final NavigableMap<Number640, Data> dataMap, final int hopCounter,
                        final boolean isUDP, final PeerAddress peerAddress,
                        final int bucketNr) {

        FutureChannelCreator frr = peer.connectionBean().reservation()
                .create(isUDP ? 1 : 0, isUDP ? 0 : 1);
        frr.addListener(new BaseFutureAdapter<FutureChannelCreator>() {
            @Override
            public void operationComplete(final FutureChannelCreator future)
                    throws Exception {
                if (future.isSuccess()) {
                    BroadcastBuilder broadcastBuilder = new BroadcastBuilder(
                            peer, messageKey);
                    broadcastBuilder.dataMap(dataMap);
                    broadcastBuilder.hopCounter(hopCounter + 1);
                    broadcastBuilder.udp(isUDP);
                    FutureResponse futureResponse = peer.broadcastRPC()
                            .send(peerAddress, broadcastBuilder,
                                    future.channelCreator(), broadcastBuilder,
                                    bucketNr);
                    Utils.addReleaseListener(future.channelCreator(),
                            futureResponse);
                } else {
                    Utils.addReleaseListener(future.channelCreator());
                }
            }
        });
    }
}
