package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import network.elrond.core.ThreadUtil;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class P2PRequestServiceImpl implements P2PRequestService {

    private static final Logger logger = LogManager.getLogger(P2PRequestServiceImpl.class);


    @Override
    @SuppressWarnings("unchecked")
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
            futureGet.awaitUninterruptibly(1000);
            if (futureGet.isSuccess() && !futureGet.isEmpty()) {

                //filter the channel peers
                HashSet<PeerAddress> peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap()
                        .values()
                        .iterator()
                        .next()
                        .object();

                // remove self from channel peer list
                PeerAddress self = connection.getPeer().peerAddress();
                peersOnChannel.remove(self);

                List<R> responses = new ArrayList<>();
                Peer peer = dht.peer();

                List<DirectBaseFutureListener> listOfFutureGets = new ArrayList<>();

                peersOnChannel.stream().parallel().forEach(peerAddress -> {
                    FutureDirect futureDirect = peer
                            .sendDirect(peerAddress)
                            .object(new P2PRequestMessage(key, channelName, shard)).start();

                    DirectBaseFutureListener<FutureGet> directBaseFutureListener = new DirectBaseFutureListener<>();

                    futureDirect.addListener(directBaseFutureListener);

                    synchronized (listOfFutureGets){
                        listOfFutureGets.add(directBaseFutureListener);
                    }
                });

                long maxWaitTimeToMonitorResponses = 1000;
                long startTimeStamp = System.currentTimeMillis();

                while (startTimeStamp + maxWaitTimeToMonitorResponses > System.currentTimeMillis()) {
                    ThreadUtil.sleep(1);

                    synchronized (listOfFutureGets) {
                        //not sent to all
                        if (listOfFutureGets.size() != peersOnChannel.size()) {
                            continue;
                        }

                        //got all responses, not waiting
                        boolean isDone = true;
                        for (DirectBaseFutureListener directBaseFutureListener : listOfFutureGets) {
                            if (directBaseFutureListener.getObject() == null) {
                                isDone = false;
                                break;
                            }
                        }

                        if (isDone) {
                            break;
                        }
                    }
                }

                synchronized (listOfFutureGets){
                    for (DirectBaseFutureListener directBaseFutureListener : listOfFutureGets) {
                        if (directBaseFutureListener.getObject() != null) {
                            responses.add((R)directBaseFutureListener.getObject());
                        }
                    }
                }

                if (responses.isEmpty()) {
                    return logger.traceExit((R) null);
                }

                Map<R, String> objectToHash = responses.stream().collect(
                        Collectors.toMap(
                                response -> response,
                                response -> AppServiceProvider.getSerializationService().getHashString(response),
                                (response1, response2) -> response1));

                Map<String, Long> counts = objectToHash.entrySet()
                        .stream()
                        .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.counting()));

                String element = Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();

                List<R> results = objectToHash.entrySet()
                        .stream()
                        .filter(entry -> Objects.equals(entry.getValue(), element))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                R result = (results.size() > 0) ? results.get(0) : null;

                return logger.traceExit(result);
            }
        } catch (Exception e) {
            logger.catching(e);
        }
        return logger.traceExit((R) null);
    }
}
