package network.elrond.p2p;


import net.tomp2p.connection.DSASignatureFactory;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.StorageDisk;
import network.elrond.application.AppContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class P2PConnectionServiceImpl implements P2PConnectionService {

    private static final Logger logger = LogManager.getLogger(P2PConnectionServiceImpl.class);
    
    private static final int TTL_CHECK_INTERVAL_MS = StorageMemory.DEFAULT_STORAGE_CHECK_INTERVAL;
    private static final int MAX_VERSIONS_HISTORY = 5;

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
        Peer peer = new PeerBuilder(Number160.createHash(nodeName)).ports(peerPort).start();
//        PeerDHT dht = new PeerBuilderDHT(peer).start();

        // Number160 peerId, File path, SignatureFactory signatureFactory
        StorageDisk storage = new StorageDisk(Number160.createHash(nodeName), new File("./"), new DSASignatureFactory());
        PeerDHT dht = new PeerBuilderDHT(peer)
                .storage(storage)
                //.storageLayer(storageMemory)
                .start();

        FutureBootstrap fb = peer
                .bootstrap()
                .inetAddress(InetAddress.getByName(masterPeerIpAddress))
                .ports(masterPeerPort).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
            logger.info("Connection was SUCCESSFUL! Status: {}", fb.failedReason());
        } else {
            RuntimeException ex = new RuntimeException(fb.failedReason());
            logger.throwing(ex);
            throw ex;
        }

        P2PConnection connection = new P2PConnection(nodeName, peer, dht);
        return logger.traceExit(connection);
    }


}
