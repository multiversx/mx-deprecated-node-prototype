package network.elrond.p2p;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import network.elrond.application.AppContext;
import network.elrond.sharding.Shard;
import network.elrond.p2p.handlers.BroadcastStructuredHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.NavigableMap;
import java.util.TreeMap;

public class P2PConnectionServiceImpl implements P2PConnectionService {

    private static final Logger logger = LogManager.getLogger(P2PConnectionServiceImpl.class);

    public P2PConnection createConnection(AppContext context) throws IOException {

        String nodeName = context.getNodeName();
        int peerPort = context.getPort();
        String masterPeerIpAddress = context.getMasterPeerIpAddress();
        int masterPeerPort = context.getMasterPeerPort();

        return createConnection(nodeName, peerPort, masterPeerIpAddress, masterPeerPort);

    }

    public P2PConnection createConnection(String nodeName,
                                          int peerPort,
                                          String masterPeerIpAddress,
                                          int masterPeerPort) throws IOException {
        logger.traceEntry("params: {} {} {} {}", nodeName, peerPort, masterPeerIpAddress, masterPeerPort);

        BroadcastStructuredHandler broadcastStructuredHandler = new BroadcastStructuredHandler();

        Peer peer = new PeerBuilder(Number160.createHash(nodeName)).broadcastHandler(broadcastStructuredHandler).ports(peerPort).start();
        PeerDHT dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer
                .bootstrap()
                .inetAddress(InetAddress.getByName(masterPeerIpAddress))
                .ports(masterPeerPort).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
            logger.info("Connection was SUCCESSFUL! Status: {}", fb.failedReason());
        } else {
            logger.error(fb.failedReason());
        }

        P2PConnection connection = new P2PConnection(nodeName, peer, dht);
        peer.objectDataReply(connection.getDataReplyCallback());
        connection.setBroadcastStructuredHandler(broadcastStructuredHandler);
        return logger.traceExit(connection);
    }

    public void introduceSelf(Shard shard, P2PConnection connection) {
        Peer peer = connection.getPeer();
        NavigableMap<Number640, Data> messageData = new TreeMap<>();
        try {
            messageData.put(Number640.ZERO, new Data(new P2PIntroductionMessage(peer.peerAddress(), shard.getIndex())));
            peer.broadcast(Number160.createHash(peer.peerID().toString())).dataMap(messageData).start();
        } catch (IOException e) {
            logger.catching(e);
        }
    }
}
