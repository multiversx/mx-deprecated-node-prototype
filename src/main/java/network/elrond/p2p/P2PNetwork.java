package network.elrond.p2p;


import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Main TomDB static class to createConnection the peers and get the connection to the database API.
 */
public class P2PNetwork {
    private static final Logger logger = LoggerFactory.getLogger(P2PNetwork.class);
    /**
     * Database peer.
     */
    private static P2PNetworkConnection networkPeer;
    /**
     * Local DHT peers.
     */
    private static Peer[] peers = new Peer[0];
    /**
     * Default port.
     */
    private static int port = 4000;
    private static Random rnd = new Random();

    /**
     * Start TomDB as the bootstrapping peer. (non-API)
     */
    public static void startDHT() {
        if (peers.length == 0) {
            createLocalPeers(1, false);
            logger.info("Succesfully created the bootstrapping peer with the address {} and port {}!", peers[0].peerAddress().inetAddress().getHostAddress(), peers[0].peerAddress().udpPort());
        } else {
            logger.info("The bootstrapping peer has the address {} and port {}!", peers[0].peerAddress().inetAddress().getHostAddress(), peers[0].peerAddress().udpPort());
        }
    }

    /**
     * Start a peer and bootstrap to the given address. (non-API)
     * If randomPort is true, a random port is chosen to avoid conflicts if many peers are executed on the same machine.
     *
     * @param bootstrapAddress
     * @param randomPort
     */
    public static void startDHT(String bootstrapAddress, boolean randomPort) {
        if (peers.length == 0) {
            createLocalPeers(1, randomPort);
            bootstrap(bootstrapAddress);
            logger.info("Succesfully created and bootstrapped one peer!");
        } else {
            bootstrap(bootstrapAddress);
            logger.info("Succesfully bootstrapped local peers!");
        }
    }

    /**
     * Start TomDB as the bootstrapping peer and crates a DB peer, returning the connection object. (API)
     */
    public static P2PNetworkConnection getConnection() {
        if (networkPeer == null) {
            if (peers.length == 0) {
                createLocalPeers(1, false);
                networkPeer = new P2PNetworkConnection(peers);
                logger.info("Succesfully created the bootstrapping peer and the DB peer with the address {} and port {}!",
                        peers[0].peerAddress().inetAddress().getHostAddress(), peers[0].peerAddress().udpPort());
            } else {
                networkPeer = new P2PNetworkConnection(peers);
                logger.info("Succesfully created the DB peer and the bootstrapping peer has the address {} and port {}!",
                        peers[0].peerAddress().inetAddress().getHostAddress(), peers[0].peerAddress().udpPort());
            }
            return networkPeer;
        } else {
            logger.info("DB peer already created!");
            return networkPeer;
        }
    }

    /**
     * Start a peer and bootstrap to the given address, start a DB peer returning the connection object. (API)
     * If randomPort is true, a random port is chosen to avoid conflicts if many peers are executed on the same machine.
     *
     * @param bootstrapAddress
     * @param randomPort
     */
    public static P2PNetworkConnection getConnection(String bootstrapAddress, boolean randomPort) {
        if (networkPeer == null) {
            if (peers.length == 0) {
                createLocalPeers(1, randomPort);
                bootstrap(bootstrapAddress);
                networkPeer = new P2PNetworkConnection(peers);
                logger.info("Successful created and bootstrapped one peer and created the DB peer!");
            } else {
                bootstrap(bootstrapAddress);
                networkPeer = new P2PNetworkConnection(peers);
                logger.info("Successful bootstrapped local peers and created the DB peer!");
            }
            return networkPeer;
        } else {
            logger.info("DB peer already created!");
            return networkPeer;
        }
    }

    /**
     * Bootstrap to the given address on default port 4000.
     *
     * @param inetAddress
     */
    private static void bootstrap(String inetAddress) {
        FutureBootstrap fb = null;
        try {
            fb = peers[0].bootstrap().inetAddress(Inet4Address.getByName(inetAddress)).ports(4000).start();
        } catch (UnknownHostException e) {
            logger.error("InetAddress conversion failed!", e);
        }
        fb.awaitUninterruptibly();
        if (fb.isFailed()) {
            logger.debug("Bootstrap failed!");
        } else {
            logger.debug("Bootstrap successfull!");
            if (fb.bootstrapTo() != null) {
                peers[0].discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
            }
        }

    }

    /**
     * Shutdown the peers and exits.
     */
    public static void stopDHT() {
        peers[0].shutdown();
        logger.debug("Closing peers down...");
        System.exit(0);
    }

    /**
     * Create n local peers on the same port.
     *
     * @param num
     */
    public static void createLocalPeers(int num, boolean randomPort) {
        if (randomPort) {
            port = 4000 + (rnd.nextInt() % 1000);
        }
        peers = new Peer[num];
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                try {
                    peers[0] = new PeerBuilder(new Number160(rnd)).ports(port).start();
                } catch (IOException ex) {
                    logger.error("Failed to start a new Peer", ex);
                }
            } else {
                try {
                    peers[i] = new PeerBuilder(new Number160(rnd)).masterPeer(peers[0]).start();
                } catch (IOException ex) {
                    logger.error("Failed to start a new Peer", ex);
                }
            }
        }
        if (peers.length > 1) {
            for (int i = 0; i < num; i++) {
                for (int j = 0; j < num; j++) {
                    peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
                }
            }
        }
        logger.debug("Successfully started {} local Peers!", num);
    }

}
