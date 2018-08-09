package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class P2PBroadcastServiceImpl implements P2PBroadcastService {

    private static final Logger logger = LogManager.getLogger(P2PBroadcastServiceImpl.class);

    private List<String> getChannelIdentifiers(P2PConnection connection, P2PBroadcastChannelName channelName) {
        P2PBroadcastChannel channel = new P2PBroadcastChannel(channelName, connection);
        List<String> channelIds = new ArrayList<>();

        Integer nbShards = AppServiceProvider.getShardingService().getNumberOfShards();
        Integer currentShard = connection.getShard().getIndex();

        if (channelName.getType().equals(P2PChannelType.GLOBAL_LEVEL)) {
            for (Integer idx = 0; idx < nbShards; idx++) {
                if (!idx.equals(currentShard)) {
                    channelIds.add(channel.getChannelIdentifier(idx));
                }
            }
        } else {
            channelIds.add(channel.getChannelIdentifier(connection.getShard().getIndex()));
        }
        return channelIds;
    }

    public P2PBroadcastChannel createChannel(P2PConnection connection, P2PBroadcastChannelName channelName) {
        logger.traceEntry("params: {} {}", connection, channelName);

        PeerDHT dht = connection.getDht();
        logger.trace("got connection...");

        P2PBroadcastChannel channel = new P2PBroadcastChannel(channelName, connection);
        List<String> channelIds = getChannelIdentifiers(connection, channelName);

        for (String channelId : channelIds) {
            subscribeToChannel(connection, channelId);
        }
        Peer peer = connection.getPeer();
        peer.objectDataReply(connection.registerChannel(channel));

        return logger.traceExit(channel);
    }

    @SuppressWarnings("unchecked")
    public boolean subscribeToChannel(P2PBroadcastChannel channel) {
        logger.traceEntry("params: {}", channel);
        List<String> channelIds = getChannelIdentifiers(channel.getConnection(), channel.getName());
        boolean result = true;
        P2PConnection connection = channel.getConnection();

        for (String channelId : channelIds) {
            boolean resultSubscribed = subscribeToChannel(connection, channelId);
            result = result & resultSubscribed;
        }
        return logger.traceExit(result);
    }

    @SuppressWarnings("unchecked")
    private boolean subscribeToChannel(P2PConnection connection, String channelId) {
        Number160 hash = Number160.createHash(channelId);

        HashSet<PeerAddress> hashSetPeers = new HashSet<>();

        try {
            PeerDHT dht = connection.getDht();
            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly(3000);
            if (!futureGet.isSuccess()) {
                logger.warn("Error getting subscribed peers from broadcast channel {}: {}", channelId, futureGet.failedReason());
                return false;
            }

            if (futureGet.dataMap().values().iterator().hasNext()) {
                hashSetPeers.addAll((HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object());
            }

            hashSetPeers.add(connection.getPeer().peerAddress());
            dht.put(hash).data(new Data(hashSetPeers)).start().awaitUninterruptibly();

            logger.debug("Added self to broadcast channel {}", channelId);
            return (true);
        } catch (Exception ex) {
            logger.catching(ex);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel channel) {
        logger.traceEntry("params: {}", channel);
        HashSet<PeerAddress> totalPeers = new HashSet<>();
        List<String> channelIds = getChannelIdentifiers(channel.getConnection(), channel.getName());
        P2PConnection connection = channel.getConnection();
        PeerDHT dht = connection.getDht();
        logger.trace("got connection...");

        for (String channelId : channelIds) {
            Number160 hash = Number160.createHash(channelId);
            HashSet<PeerAddress> tempPeers = new HashSet<>();
            try {
                FutureGet futureGet = dht.get(hash).start();
                futureGet.awaitUninterruptibly(3000);
                if (!futureGet.isSuccess()) {
                    logger.warn("Error getting subscribed peers from broadcast channel {}", channelId);
                } else {
                    if (futureGet.dataMap().values().iterator().hasNext()) {
                        tempPeers.addAll((HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object());
                    }

                    if (!tempPeers.contains(connection.getPeer().peerAddress())) {
                        logger.warn("Not found self on broadcast channel {}...re-adding", channelId);

                        subscribeToChannel(connection, channelId);
                        tempPeers.add(connection.getPeer().peerAddress());
                    }
                }

                channel.addPeerAddresses(hash.toString(), tempPeers);

                totalPeers.addAll(channel.getPeerAddresses(hash.toString()));
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }

        if (!totalPeers.contains(dht.peer().peerAddress())) {
            logger.fatal("Not found self on channel!");
        }

        return logger.traceExit(totalPeers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel globalChannel, Integer destinationShard) {
        logger.traceEntry("params: {}", globalChannel);
        String channelId = globalChannel.getChannelIdentifier(destinationShard);
        P2PConnection connection = globalChannel.getConnection();
        //hash object where peers are subscribed
        Number160 hash = Number160.createHash(channelId);

        HashSet<PeerAddress> tempPeers = new HashSet<>();
        try {
            PeerDHT dht = connection.getDht();
            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly(3000);
            if (!futureGet.isSuccess()) {
                logger.warn("Error getting subscribed peers from broadcast channel {}", channelId);
            } else {
                if (futureGet.dataMap().values().iterator().hasNext()) {
                    tempPeers.addAll((HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object());
                }

                if (!tempPeers.contains(connection.getPeer().peerAddress())) {
                    logger.warn("Not found self on broadcast channel {}...re-adding", channelId);

                    subscribeToChannel(connection, channelId);
                    tempPeers.add(connection.getPeer().peerAddress());
                }
            }

            globalChannel.addPeerAddresses(hash.toString(), tempPeers);

        } catch (Exception ex) {
            logger.catching(ex);
        }

        return globalChannel.getPeerAddresses(hash.toString());
    }

    @Override
    public void addPeerToShardBucket(PeerAddress peerAddress, Integer shard) {
        logger.traceEntry("params: {} {}", peerAddress, shard);

        logger.traceExit();
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean publishToChannel(P2PBroadcastChannel channel, Serializable object, Integer destinationShard) {
        logger.traceEntry("params: {} {}", channel, object);

        HashSet<PeerAddress> peersOnChannel = getPeersOnChannel(channel, destinationShard);

        try {
            P2PConnection connection = channel.getConnection();
            P2PBroadcastChannelName channelName = channel.getName();

            PeerDHT dht = connection.getDht();

            // send in parallel
            peersOnChannel.stream().parallel().forEach(peerAddress -> dht.peer()
                    .sendDirect(peerAddress)
                    .object(new P2PBroadcastMessage(channelName, object))
                    .start());

            logger.trace("published to channel!");
            return logger.traceExit(true);
        } catch (Exception ex) {
            logger.catching(ex);
        }
        return logger.traceExit(false);
    }


    @SuppressWarnings("unchecked")
    public boolean unsubscribeFromChannel(P2PBroadcastChannel channel) {
        logger.traceEntry("params: {}", channel);

        List<String> channelIds = getChannelIdentifiers(channel.getConnection(), channel.getName());
        P2PConnection connection = channel.getConnection();
        PeerDHT dht = connection.getDht();
        boolean result = true;

        logger.trace("got connection...");


        for (String channelId : channelIds) {
            try {
                //hash object where peers are subscribed
                Number160 hash = Number160.createHash(channelId);

                //the version where to store data
                Number160 version = connection.getPeer().peerID();
                dht.remove(hash).versionKey(version).start().awaitUninterruptibly();

                logger.debug("unsubscribed from channel: {}, peer: {}!", channelId, connection.getPeer().peerAddress());

                result = true;
            } catch (Exception ex) {
                logger.catching(ex);
                result = false;
            }
        }
        return logger.traceExit(result);
    }

    public boolean leaveNetwork(List<P2PBroadcastChannel> channels) {
        logger.traceEntry("params: {}", channels);
        P2PConnection connection = channels.get(0).getConnection();

        for (P2PBroadcastChannel channel : channels) {
            unsubscribeFromChannel(channel);
        }

        PeerDHT dht = connection.getDht();
        dht.peer().announceShutdown().start().awaitUninterruptibly();
        logger.trace("left network!");
        return logger.traceExit(true);
    }
}