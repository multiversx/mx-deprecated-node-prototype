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
        P2PBroadcastChanel channel = new P2PBroadcastChanel(channelName, connection);
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

    public P2PBroadcastChanel createChannel(P2PConnection connection, P2PBroadcastChannelName channelName) {
        logger.traceEntry("params: {} {}", connection, channelName);

        PeerDHT dht = connection.getDht();
        logger.trace("got connection...");

        P2PBroadcastChanel channel = new P2PBroadcastChanel(channelName, connection);
        List<String> channelIds = getChannelIdentifiers(connection, channelName);

        for (String channelId : channelIds) {
            try {
                Number160 hash = Number160.createHash(channelId);

                FutureGet futureGet = dht.get(hash).start();
                futureGet.awaitUninterruptibly();
                if (futureGet.isSuccess() && futureGet.isEmpty()) {
                    dht.put(hash)
                            .data(new Data(new HashSet<PeerAddress>()))
                            .start()
                            .awaitUninterruptibly();
                    logger.info("created new channel with id {}", channelId);
                } else {
                    logger.warn(futureGet.failedReason());
                }
            } catch (Exception e) {
                logger.catching(e);
                return logger.traceExit((P2PBroadcastChanel) null);
            }
        }
        Peer peer = connection.getPeer();
        peer.objectDataReply(connection.registerChannel(channel));

        return logger.traceExit(channel);
    }

    @SuppressWarnings("unchecked")
    public boolean subscribeToChannel(P2PBroadcastChanel channel) {
        logger.traceEntry("params: {}", channel);
        List<String> channelIds = getChannelIdentifiers(channel.getConnection(), channel.getName());
        boolean result = true;
        P2PConnection connection = channel.getConnection();
        PeerDHT dht = connection.getDht();
        logger.trace("got connection...");

        for (String channelId : channelIds) {

            Number160 hash = Number160.createHash(channelId);

            try {
                FutureGet futureGet = dht.get(hash).start();
                futureGet.awaitUninterruptibly();

                if (futureGet.isSuccess()) {
                    if (futureGet.isEmpty()) {
                        result = false;
                        continue;
                    }
                    HashSet<PeerAddress> peersOnChannel;
                    peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                    peersOnChannel.add(dht.peer().peerAddress());
                    dht.put(hash).data(new Data(peersOnChannel)).start().awaitUninterruptibly();

                    logger.info("subscribed to channel {}", channelId);
                }
            } catch (Exception e) {
                logger.catching(e);
                result = false;
            }
        }
        return logger.traceExit(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChanel channel) {
        logger.traceEntry("params: {}", channel);
        HashSet<PeerAddress> totalPeers = new HashSet<>();
        List<String> channelIds = getChannelIdentifiers(channel.getConnection(), channel.getName());
        P2PConnection connection = channel.getConnection();
        PeerDHT dht = connection.getDht();
        logger.trace("got connection...");

        for (String channelId : channelIds) {
            try {
                Number160 hash = Number160.createHash(channelId);

                FutureGet futureGet = dht.get(hash).start();
                futureGet.awaitUninterruptibly();
                if (futureGet.isSuccess()) {
                    HashSet<PeerAddress> peersOnChannel;
                    peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();

                    if (peersOnChannel == null || peersOnChannel.isEmpty()) {
                        logger.debug("peers on channel {} for channel ID: {}", channelId);
                        continue;
                    }
                    totalPeers.addAll(peersOnChannel);
                } else {
                    logger.warn(futureGet.failedReason());
                }
            } catch (Exception e) {
                logger.catching(e);
            }
        }
        return logger.traceExit(totalPeers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChanel globalChannel, Integer destinationShard) {
        logger.traceEntry("params: {}", globalChannel);
        try {
            P2PConnection connection = globalChannel.getConnection();

            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            String channelIdentifier = globalChannel.getChannelIdentifier(destinationShard);
            Number160 hash = Number160.createHash(channelIdentifier);

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();

                if (peersOnChannel == null || peersOnChannel.isEmpty()) {
                    logger.debug("peers on channel: {} for channel ID: {}", channelIdentifier);
                }

                return logger.traceExit(peersOnChannel);
            } else {
                logger.warn(futureGet.failedReason());
            }
        } catch (Exception e) {
            logger.catching(e);
        }

        return logger.traceExit((HashSet<PeerAddress>) null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean publishToChannel(P2PBroadcastChanel channel, Serializable object, Integer destinationShard) {
        logger.traceEntry("params: {} {}", channel, object);
        try {
            P2PConnection connection = channel.getConnection();
            P2PBroadcastChannelName channelName = channel.getName();

            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            Number160 hash = Number160.createHash(channel.getChannelIdentifier(destinationShard));

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();

                // send in parallel
                peersOnChannel.stream().parallel().forEach(peerAddress -> dht.peer()
                        .sendDirect(peerAddress)
                        .object(new P2PBroadcastMessage(channelName, object))
                        .start());

                logger.trace("published to channel!");
                return logger.traceExit(true);
            }
        } catch (Exception e) {
            logger.catching(e);
        }
        return logger.traceExit(false);
    }


    @SuppressWarnings("unchecked")
    public boolean unsubscribeFromChannel(P2PBroadcastChanel channel) {
        logger.traceEntry("params: {}", channel);
        List<String> channelIds = getChannelIdentifiers(channel.getConnection(), channel.getName());
        P2PConnection connection = channel.getConnection();
        PeerDHT dht = connection.getDht();
        boolean result = true;

        logger.trace("got connection...");


        for (String channelId : channelIds) {
            try {
                Number160 hash = Number160.createHash(channelId);

                FutureGet futureGet = dht.get(hash).start();
                futureGet.awaitUninterruptibly();
                if (futureGet.isSuccess()) {
                    if (futureGet.isEmpty()) {
                        result = false;
                    }
                    HashSet<PeerAddress> peersOnChannel;
                    peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                    peersOnChannel.remove(dht.peer().peerAddress());
                    dht.put(hash).data(new Data(peersOnChannel)).start().awaitUninterruptibly();
                    logger.trace("unsubscribed from channel!");
                }
            } catch (Exception e) {
                logger.catching(e);
                result = false;
            }
        }
        return logger.traceExit(result);
    }

    public boolean leaveNetwork(List<P2PBroadcastChanel> channels) {
        logger.traceEntry("params: {}", channels);
        P2PConnection connection = channels.get(0).getConnection();

        for (P2PBroadcastChanel chanel : channels) {
            unsubscribeFromChannel(chanel);
        }

        PeerDHT dht = connection.getDht();
        dht.peer().announceShutdown().start().awaitUninterruptibly();
        logger.trace("left network!");
        return logger.traceExit(true);
    }
}