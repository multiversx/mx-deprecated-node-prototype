package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashSet;

public class P2PRequestServiceImpl implements P2PRequestService {

    private static final Logger logger = LogManager.getLogger(P2PRequestServiceImpl.class);


    @Override
    public P2PRequestChannel createChannel(P2PConnection connection, Shard shard, P2PRequestChannelName channelName) {
        logger.traceEntry("params: {} {}", connection, channelName);
        try {
            PeerDHT dht = connection.getDht();
            logger.trace("got connection...");

            P2PRequestChannel channel = new P2PRequestChannel(channelName, connection);

            Number160 hash = Number160.createHash(channel.getChannelIdentifier(shard));

            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {

                if (futureGet.isEmpty()) {
                    // Create new
                    HashSet<PeerAddress> peersOnChannel = new HashSet<>();
                    peersOnChannel.add(dht.peer().peerAddress());
                    dht.put(hash).data(new Data(peersOnChannel)).start().awaitUninterruptibly();
                } else {
                    // Addon existing one
                    HashSet<PeerAddress> peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                    peersOnChannel.add(dht.peer().peerAddress());
                    dht.put(hash).data(new Data(peersOnChannel)).start().awaitUninterruptibly();
                }

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

        return logger.traceExit((P2PRequestChannel) null);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <K extends Serializable, R extends Serializable> R get(P2PRequestChannel channel, Shard shard, P2PRequestChannelName channelName, K key) {

        logger.traceEntry("params: {} {} {} {}", channel, shard, channelName, key);
        try {

            P2PConnection connection = channel.getConnection();
            PeerDHT dht = connection.getDht();
            Number160 hash = Number160.createHash(channel.getChannelIdentifier(shard));


            FutureGet futureGet = dht.get(hash).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess() && !futureGet.isEmpty()) {

                HashSet<PeerAddress> peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();

                for (PeerAddress peer : peersOnChannel) {
                    FutureDirect futureDirect = dht.peer()
                            .sendDirect(peer)
                            .object(new P2PRequestMessage(key, channelName, shard))
                            .start();
                    FutureDirect fd = futureDirect.awaitUninterruptibly();

                    if (fd.isCompleted() && fd.isSuccess()) {
                        return logger.traceExit((R) fd.object());
                    }
                }

            }
        } catch (Exception e) {
            logger.catching(e);
        }
        return logger.traceExit((R) null);
    }


}
