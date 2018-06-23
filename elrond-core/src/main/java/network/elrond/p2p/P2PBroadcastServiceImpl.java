package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;


public class P2PBroadcastServiceImpl implements P2PBroadcastService {

    private static final Logger logger = LogManager.getLogger(P2PBroadcastServiceImpl.class);


    public P2PBroadcastChanel createChannel(P2PConnection connection, P2PChannelName channelName) {
        logger.traceEntry("params: {} {}", connection, channelName);
        try {
            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            P2PBroadcastChanel channel = new P2PBroadcastChanel(channelName, connection);
            Number160 hash = Number160.createHash(channel.getChannelIdentifier());

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess() && futureGet.isEmpty()) {
                dht.put(hash)
                        .data(new Data(new HashSet<PeerAddress>()))
                        .start()
                        .awaitUninterruptibly();
                logger.trace("created new channel");
            } else {
                logger.warn(futureGet.failedReason());
            }

            Peer peer = connection.getPeer();
            peer.objectDataReply(connection.registerChannel(channel));

            return logger.traceExit(channel);
        } catch (Exception e) {
            logger.catching(e);
        }

        return logger.traceExit((P2PBroadcastChanel) null);
    }

    @SuppressWarnings("unchecked")
    public boolean subscribeToChannel(P2PBroadcastChanel channel) {
        logger.traceEntry("params: {}", channel);

        Number160 hash = Number160.createHash(channel.getChannelIdentifier());

        try {
            P2PConnection connection = channel.getConnection();
            P2PChannelName channelName = channel.getName();
            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) return false;
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                peersOnChannel.add(dht.peer().peerAddress());
                dht.put(hash).data(new Data(peersOnChannel)).start().awaitUninterruptibly();

                logger.trace("subscribed to channel!");

                return logger.traceExit(true);
            }
        } catch (Exception e) {
            logger.catching(e);
        }
        return logger.traceExit(false);
    }


    @Override
    public HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChanel channel) {
        logger.traceEntry("params: {}", channel);
        try {
            P2PConnection connection = channel.getConnection();

            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            String channelIdentifier = channel.getChannelIdentifier();
            Number160 hash = Number160.createHash(channelIdentifier);

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                return logger.traceExit(peersOnChannel);
            }else{
                logger.warn(futureGet.failedReason());
            }
        } catch (Exception e) {
            logger.catching(e);
        }

        return logger.traceExit((HashSet<PeerAddress>) null);

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean publishToChannel(P2PBroadcastChanel channel, Serializable object) {
        logger.traceEntry("params: {} {}", channel, object);
        try {
            P2PConnection connection = channel.getConnection();
            P2PChannelName channelName = channel.getName();

            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            Number160 hash = Number160.createHash(channel.getChannelIdentifier());

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                for (PeerAddress peer : peersOnChannel) {
                    FutureDirect futureDirect = dht.peer()
                            .sendDirect(peer)
                            .object(new P2PBroadcastMessage(channelName, object))
                            .start();
                    futureDirect.awaitUninterruptibly();
                }
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
        try {

            P2PConnection connection = channel.getConnection();


            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            Number160 hash = Number160.createHash(channel.getChannelIdentifier());

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) return false;
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                peersOnChannel.remove(dht.peer().peerAddress());
                dht.put(hash).data(new Data(peersOnChannel)).start().awaitUninterruptibly();
                logger.trace("unsubscribed from channel!");
                return logger.traceExit(true);
            }
        } catch (Exception e) {
            logger.catching(e);
        }
        return logger.traceExit(false);
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