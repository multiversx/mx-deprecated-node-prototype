package network.elrond.p2p.service;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import network.elrond.application.AppContext;
import network.elrond.p2p.handlers.BroadcastStructuredHandler;
import network.elrond.p2p.model.P2PConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

public class P2PConnectionServiceImpl implements P2PConnectionService {

    private static final Logger logger = LogManager.getLogger(P2PConnectionServiceImpl.class);

    @Override
	public P2PConnection createConnection(AppContext context) throws IOException {

        String nodeName = context.getNodeName();
        int peerPort = context.getPort();
        String masterPeerIpAddress = context.getMasterPeerIpAddress();
        int masterPeerPort = context.getMasterPeerPort();

        return createConnection(nodeName, peerPort, masterPeerIpAddress, masterPeerPort);

    }

    @Override
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
            throw new RuntimeException(fb.failedReason());
        }

        P2PConnection connection = new P2PConnection(nodeName, peer, dht);
        connection.setBroadcastHandler(broadcastStructuredHandler);

        peer.objectDataReply(connection.getDataReplyCallback());
        broadcastStructuredHandler.setConnection(connection);

        return logger.traceExit(connection);
    }

    @Override
	public <T extends Serializable> void broadcastMessage(T object, P2PConnection connection) {
        Peer peer = connection.getPeer();
        NavigableMap<Number640, Data> messageData = new TreeMap<>();
        try {
            messageData.put(Number640.ZERO, new Data(object));
            peer.broadcast(Number160.createHash(UUID.randomUUID().toString())).dataMap(messageData).start();
        } catch (IOException e) {
            logger.catching(e);
        }
    }
}
