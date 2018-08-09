package network.elrond.p2p.handlers;

import net.tomp2p.futures.*;
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
import network.elrond.p2p.P2PIntroductionMessage;
import network.elrond.p2p.P2PReplyIntroductionMessage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public AppState getAppState() {
        return appState;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    @Override
    public StructuredBroadcastHandler receive(Message message) {
        //sanity checks
        if (peer == null) {
            return this;
        }

        if (message == null) {
            return this;
        }

        if (appState == null) {
            return this;
        }

        final PeerAddress sender = message.sender();

        final Number160 messageKey = message.key(0);
        //filter out same message processed
        if (twiceSeen(messageKey)) {
            //logger.debug("{} already received the message: {}", peer.peerAddress().tcpPort(), messageKey);
            return this;
        }
        NavigableMap<Number640, Data> dataMap;
        PeerAddress peerAddressReceived;

        if (message.dataMap(0) != null) {
            dataMap = message.dataMap(0).dataMap();
            try {
                P2PIntroductionMessage introductionMessage = (P2PIntroductionMessage) dataMap.get(Number640.ZERO).object();
                logger.debug("{} received broadcast message from: {}", peer.peerID(), introductionMessage.getPeerAddress());
                peerAddressReceived = introductionMessage.getPeerAddress();
                appState.addPeerOnShard(peerAddressReceived, introductionMessage.getShardId());

            } catch (ClassNotFoundException | IOException e) {
                logger.catching(e);
                return this;
            }
        } else {
            return this;
        }

        //broadcast
        final int hopCount = message.intAt(0);
        //get all verified peers
        List<PeerAddress> listToSend = peer.peerBean().peerMap().all();
        //to be checked if we send to the verified peers or all known peers (even overflowed peers)
        //listToSend.addAll(peer.peerBean().peerMap().allOverflow());

        for (PeerAddress peerAddress : listToSend) {
            if (peerAddress == sender) {
                //not returning to sender
                continue;
            }

            int bucketNr = PeerMap.classMember(peerAddress.peerId(),
                    peer.peerID());
            //magic send
            doSendBroadcast(messageKey, dataMap, hopCount, message.isUdp(), peerAddress,
                    bucketNr);
        }

        if (!peer.peerAddress().equals(peerAddressReceived)) {
            //get all currently known peers and send to requester

            Map<Integer, HashSet<PeerAddress>> peersMap = appState.getAllPeers();

            P2PReplyIntroductionMessage replyIntroductionMessage = new P2PReplyIntroductionMessage(peersMap);

            FutureDirect futureDirect = peer.sendDirect(peerAddressReceived).object(replyIntroductionMessage).start();
            futureDirect.addListener(new BaseFutureAdapter<BaseFuture>() {
                @Override
                public void operationComplete(BaseFuture future) throws Exception {
                    if (future.isSuccess() && future.isCompleted()){
                        logger.debug("Done sending to {} the bucket", peerAddressReceived);
                    } else {
                        logger.warn("Error sending to {}: {}", peerAddressReceived, future.failedReason());
                    }
                }
            });
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

    private void doSendBroadcast(final Number160 messageKey,
                                 final NavigableMap<Number640, Data> dataMap, final int hopCounter,
                                 final boolean isUDP, final PeerAddress peerAddress,
                                 final int bucketNr) {

        FutureChannelCreator frr = peer.connectionBean().reservation()
                .create(isUDP ? 1 : 0, isUDP ? 0 : 1);
        frr.addListener(new BaseFutureAdapter<FutureChannelCreator>() {
            @Override
            public void operationComplete(final FutureChannelCreator future) {
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
