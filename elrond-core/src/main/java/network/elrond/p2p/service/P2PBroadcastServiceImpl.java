package network.elrond.p2p.service;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import network.elrond.p2p.model.P2PBroadcastChannel;
import network.elrond.p2p.model.P2PBroadcastChannelName;
import network.elrond.p2p.model.P2PBroadcastMessage;
import network.elrond.p2p.model.P2PChannelType;
import network.elrond.p2p.model.P2PConnection;
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

    @Override
	public P2PBroadcastChannel createChannel(P2PConnection connection, P2PBroadcastChannelName channelName) {
        logger.traceEntry("params: {} {}", connection, channelName);

        PeerDHT dht = connection.getDht();
        logger.trace("got connection...");

        P2PBroadcastChannel channel = new P2PBroadcastChannel(channelName, connection);
        List<String> channelIds = getChannelIdentifiers(connection, channelName);

        Peer peer = connection.getPeer();
        peer.objectDataReply(connection.registerChannel(channel));

        return logger.traceExit(channel);
    }

    @Override
    public HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel channel) {
        logger.traceEntry("params: {}", channel);
        HashSet<PeerAddress> totalPeers = new HashSet<>();
        P2PConnection connection = channel.getConnection();
        PeerDHT dht = connection.getDht();
        logger.trace("got connection...");

        if (channel.getName().getType().equals(P2PChannelType.GLOBAL_LEVEL)) {
            for (Integer shardId : connection.getAllPeers().keySet()) {
                totalPeers.addAll(connection.getPeersOnShard(shardId));
            }
        } else {
            totalPeers.addAll(connection.getPeersOnShard(connection.getShard().getIndex()));
        }

        if (!totalPeers.contains(dht.peer().peerAddress())) {
            logger.fatal("Not found self on channel!");
        }

        return logger.traceExit(totalPeers);
    }

    @Override
    public HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel globalChannel, Integer destinationShard) {
        logger.traceEntry("params: {}", globalChannel);
        P2PConnection connection = globalChannel.getConnection();

        HashSet<PeerAddress> totalPeers = connection.getPeersOnShard(connection.getShard().getIndex());
        totalPeers.addAll(connection.getPeersOnShard(destinationShard));

        return totalPeers;
    }


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


    @Override
	public boolean unsubscribeFromChannel(P2PBroadcastChannel channel) {
        logger.traceEntry("params: {}", channel);

        List<String> channelIds = getChannelIdentifiers(channel.getConnection(), channel.getName());
        P2PConnection connection = channel.getConnection();
        boolean result = true;

        logger.trace("got connection...");


        for (String channelId : channelIds) {
            try {

                result = true;
            } catch (Exception ex) {
                logger.catching(ex);
                result = false;
            }
        }
        return logger.traceExit(result);
    }

    @Override
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