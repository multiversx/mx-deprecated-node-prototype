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
import network.elrond.blockchain.Blockchain;
import network.elrond.data.BlockHeightMessage;
import network.elrond.p2p.P2PConnection;
import network.elrond.p2p.P2PIntroductionMessage;
import network.elrond.p2p.P2PReplyIntroductionMessage;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class BroadcastStructuredHandler extends StructuredBroadcastHandler {
    private final ConcurrentCacheMap<Number160, Boolean> cache = new ConcurrentCacheMap<>();
    private volatile Peer peer;
    P2PConnection connection;
    Blockchain blockchain;

    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(BroadcastStructuredHandler.class);

    public BroadcastStructuredHandler() {
        this.peer = null;
        this.connection = null;
    }


    public void receiveIntroductionMsg(P2PIntroductionMessage introductionMessage, Peer peer) {
        logger.debug("{} received broadcast message from: {}", peer.peerID(), introductionMessage.getPeerAddress());
        PeerAddress peerAddressReceived = introductionMessage.getPeerAddress();
        connection.addPeerOnShard(peerAddressReceived, introductionMessage.getShardId());

        if (peerAddressReceived != null && !peer.peerAddress().equals(peerAddressReceived)) {
            //get all currently known peers and send to requester

            Map<Integer, HashSet<PeerAddress>> peersMap = connection.getAllPeers();

            P2PReplyIntroductionMessage replyIntroductionMessage = new P2PReplyIntroductionMessage(peersMap);
            PeerAddress peerAddress = peerAddressReceived;

            FutureDirect futureDirect = peer.sendDirect(peerAddressReceived).object(replyIntroductionMessage).start();
            futureDirect.addListener(new BaseFutureAdapter<BaseFuture>() {
                @Override
                public void operationComplete(BaseFuture future) throws Exception {
                    if (future.isSuccess() && future.isCompleted()) {
                        logger.debug("Done sending to {} the bucket", peerAddress);
                    } else {
                        logger.warn("Error sending to {}: {}", peerAddress, future.failedReason());
                    }
                }
            });
        }
    }

    public void receiveBlockHeightMessage(BlockHeightMessage blockHeightMessage, Peer peer) {
        if (blockchain != null) {
            if (connection.getShard().getIndex().equals(blockHeightMessage.getShardId())) {
                AppServiceProvider.getBootstrapService().setBlockHeightFromNetwork(blockHeightMessage.getBlockHeight(), blockchain);
                logger.debug("{} received broadcast blockHeight {}", peer.peerID(), blockHeightMessage.getBlockHeight().toString());
            }
        } else {
            logger.debug("{} received broadcast blockHeight {} but can not process because blockchain is null!",
                    peer.peerID(), blockHeightMessage.getBlockHeight().toString());
        }
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

        if (connection == null) {
            return this;
        }

        final PeerAddress sender = message.sender();

        final Number160 messageKey = message.key(0);
        //filter out same message processed
        if (twiceSeen(messageKey)) {
            return this;
        }

        NavigableMap<Number640, Data> dataMap;

        Object data;
        if (message.dataMap(0) != null) {
            dataMap = message.dataMap(0).dataMap();
            try {
                data = dataMap.get(Number640.ZERO).object();
                if (data instanceof P2PIntroductionMessage) {
                    receiveIntroductionMsg((P2PIntroductionMessage) data, peer);
                } else if (data instanceof BlockHeightMessage) {
                    receiveBlockHeightMessage((BlockHeightMessage) data, peer);
                }
            } catch (Exception e) {
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

        listToSend.stream().parallel().forEach(peerAddress -> {
            if (peerAddress == sender) {
                //not returning to sender
                return;
            }

            int bucketNr = PeerMap.classMember(peerAddress.peerId(),
                    peer.peerID());
            //magic send
            doSendBroadcast(messageKey, dataMap, hopCount, message.isUdp(), peerAddress,
                    bucketNr);
        });

        return this;
    }

    public void setConnection(P2PConnection connection) {
        this.connection = connection;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
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
