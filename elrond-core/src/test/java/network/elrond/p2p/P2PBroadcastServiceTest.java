package network.elrond.p2p;

import junit.framework.TestCase;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import network.elrond.sharding.ShardingServiceImpl;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class P2PBroadcastServiceTest {

    @Test
    public void testCreateChannel() throws Exception {
        final int nrPeers = 2;
        final int port = 4001;

        PeerDHT[] peers = ExampleUtils.createAndAttachPeersDHT(nrPeers, port);
        ExampleUtils.bootstrap(peers);

        P2PBroadcastService p2PBroadcastService = AppServiceProvider.getP2PBroadcastService();

        P2PConnection p2PConnection = new P2PConnection("dummy", peers[0].peer(), peers[0]);
        p2PConnection.setShard(new Shard(0));

        P2PBroadcastChannel p2PBroadcastChannel = p2PBroadcastService.createChannel(p2PConnection, P2PBroadcastChannelName.BLOCK);

        TestCase.assertNotNull(p2PBroadcastChannel);


        for (PeerDHT peer: peers) {
            peer.shutdown();
        }
    }

    @Test
    public void testCreateChannelAndSubscribe() throws Exception {
        final int nrPeers = 10;
        final int port = 4001;

        PeerDHT[] peers = ExampleUtils.createAndAttachPeersDHT(nrPeers, port);
        ExampleUtils.bootstrap(peers);

        P2PBroadcastService p2PBroadcastService = AppServiceProvider.getP2PBroadcastService();

        P2PConnection[] connections = new P2PConnection[peers.length];
        for (int i = 0; i < peers.length; i++){
            connections[i] = new P2PConnection("dummy" + String.valueOf(i), peers[i].peer(), peers[i]);
            connections[i].setShard(new Shard(0));
        }

        P2PBroadcastChannel[] channels = new P2PBroadcastChannel[peers.length];
        for (int i = 0; i < peers.length; i++){
            channels[i] = p2PBroadcastService.createChannel(connections[i], P2PBroadcastChannelName.BLOCK);
        }

        p2PBroadcastService.subscribeToChannel(channels[0]);
        p2PBroadcastService.subscribeToChannel(channels[4]);

        HashSet<PeerAddress> peersSubscribed = p2PBroadcastService.getPeersOnChannel(channels[0]);

        TestCase.assertEquals(2, peersSubscribed.size());
        TestCase.assertTrue(peersSubscribed.contains(channels[0].getConnection().getPeer().peerAddress()));
        TestCase.assertTrue(peersSubscribed.contains(channels[4].getConnection().getPeer().peerAddress()));
        TestCase.assertFalse(peersSubscribed.contains(channels[1].getConnection().getPeer().peerAddress()));

        for (PeerDHT peer: peers) {
            peer.shutdown();
        }
    }

    @Test
    public void testCreateChannelAndSubscribeGlobalLevel() throws Exception {
        final int nrPeers = 10;
        final int port = 4001;

        ((ShardingServiceImpl)AppServiceProvider.getShardingService()).MAX_ACTIVE_SHARDS_CONT = 10;

        PeerDHT[] peers = ExampleUtils.createAndAttachPeersDHT(nrPeers, port);
        ExampleUtils.bootstrap(peers);

        P2PBroadcastService p2PBroadcastService = AppServiceProvider.getP2PBroadcastService();

        P2PConnection[] connections = new P2PConnection[peers.length];
        for (int i = 0; i < peers.length; i++){
            connections[i] = new P2PConnection("dummy" + String.valueOf(i), peers[i].peer(), peers[i]);
            connections[i].setShard(new Shard(0));
        }

        P2PBroadcastChannel[] channels = new P2PBroadcastChannel[peers.length];
        for (int i = 0; i < peers.length; i++){
            channels[i] = p2PBroadcastService.createChannel(connections[i], P2PBroadcastChannelName.XTRANSACTION_BLOCK);
        }

        p2PBroadcastService.subscribeToChannel(channels[0]);
        p2PBroadcastService.subscribeToChannel(channels[4]);

        HashSet<PeerAddress> peersSubscribed = p2PBroadcastService.getPeersOnChannel(channels[0]);

        TestCase.assertEquals(2, peersSubscribed.size());
        TestCase.assertTrue(peersSubscribed.contains(channels[0].getConnection().getPeer().peerAddress()));
        TestCase.assertTrue(peersSubscribed.contains(channels[4].getConnection().getPeer().peerAddress()));
        TestCase.assertFalse(peersSubscribed.contains(channels[1].getConnection().getPeer().peerAddress()));

        for (PeerDHT peer: peers) {
            peer.shutdown();
        }
    }

    @Test
    public void testCreateChannelSubscribeUnsubscribe() throws Exception {
        final int nrPeers = 10;
        final int port = 4001;

        PeerDHT[] peers = ExampleUtils.createAndAttachPeersDHT(nrPeers, port);
        ExampleUtils.bootstrap(peers);

        P2PBroadcastService p2PBroadcastService = AppServiceProvider.getP2PBroadcastService();

        P2PConnection[] connections = new P2PConnection[peers.length];
        for (int i = 0; i < peers.length; i++){
            connections[i] = new P2PConnection("dummy" + String.valueOf(i), peers[i].peer(), peers[i]);
            connections[i].setShard(new Shard(0));
        }

        P2PBroadcastChannel[] channels = new P2PBroadcastChannel[peers.length];
        for (int i = 0; i < peers.length; i++){
            channels[i] = p2PBroadcastService.createChannel(connections[i], P2PBroadcastChannelName.BLOCK);
        }

        p2PBroadcastService.subscribeToChannel(channels[0]);
        p2PBroadcastService.subscribeToChannel(channels[1]);
        p2PBroadcastService.subscribeToChannel(channels[2]);
        p2PBroadcastService.subscribeToChannel(channels[4]);

        p2PBroadcastService.unsubscribeFromChannel(channels[1]);
        p2PBroadcastService.unsubscribeFromChannel(channels[3]);

        HashSet<PeerAddress> peersSubscribed = p2PBroadcastService.getPeersOnChannel(channels[0]);

        TestCase.assertEquals(3, peersSubscribed.size());
        TestCase.assertTrue(peersSubscribed.contains(channels[0].getConnection().getPeer().peerAddress()));
        TestCase.assertTrue(peersSubscribed.contains(channels[2].getConnection().getPeer().peerAddress()));
        TestCase.assertTrue(peersSubscribed.contains(channels[4].getConnection().getPeer().peerAddress()));
        TestCase.assertFalse(peersSubscribed.contains(channels[1].getConnection().getPeer().peerAddress()));

        for (PeerDHT peer: peers) {
            peer.shutdown();
        }
    }

    @Test
    public void testCreateChannelAndSubscribeConcurrency() throws Exception {
        final int nrPeers = 10;
        final int port = 4001;

        PeerDHT[] peers = ExampleUtils.createAndAttachPeersDHT(nrPeers, port);
        ExampleUtils.bootstrap(peers);

        P2PBroadcastService p2PBroadcastService = AppServiceProvider.getP2PBroadcastService();

        P2PConnection[] connections = new P2PConnection[peers.length];
        for (int i = 0; i < peers.length; i++){
            connections[i] = new P2PConnection("dummy" + String.valueOf(i), peers[i].peer(), peers[i]);
            connections[i].setShard(new Shard(0));
        }

        P2PBroadcastChannel[] channels = new P2PBroadcastChannel[peers.length];
        for (int i = 0; i < peers.length; i++){
            channels[i] = p2PBroadcastService.createChannel(connections[i], P2PBroadcastChannelName.BLOCK);
        }

        Thread thr[] = new Thread[peers.length];

        final CountDownLatch cl = new CountDownLatch(peers.length);

        for (int i = 0; i < peers.length; i++){
            final P2PBroadcastChannel p2PBroadcastChannel = channels[i];

            thr[i] = new Thread(() -> {
               p2PBroadcastService.subscribeToChannel(p2PBroadcastChannel);
               cl.countDown();
            });
        }

        for (int i = 0; i < thr.length; i++){
            thr[i].start();
        }

        cl.await();

        HashSet<PeerAddress> peersSubscribed = p2PBroadcastService.getPeersOnChannel(channels[0]);

        TestCase.assertEquals(peers.length, peersSubscribed.size());

        for (int i = 0; i < peers.length; i++){
            TestCase.assertTrue(peersSubscribed.contains(channels[i].getConnection().getPeer().peerAddress()));
        }

        for (PeerDHT peer: peers) {
            peer.shutdown();
        }
    }


    @Test
    public void testLazyConnection() throws Exception{
        PeerDHT[] peers = new PeerDHT[2];

        Random RND = new Random( 42L );
        P2PBroadcastService p2PBroadcastService = AppServiceProvider.getP2PBroadcastService();

        peers[0] = new PeerBuilderDHT(new PeerBuilder( new Number160( RND ) ).ports( 4000 ).start()).start();

        P2PConnection[] connections = new P2PConnection[peers.length];
        connections[0] = new P2PConnection("dummy" + String.valueOf(0), peers[0].peer(), peers[0]);
        connections[0].setShard(new Shard(0));

        P2PBroadcastChannel[] channels = new P2PBroadcastChannel[peers.length];
        channels[0] = p2PBroadcastService.createChannel(connections[0], P2PBroadcastChannelName.BLOCK);

        p2PBroadcastService.subscribeToChannel(channels[0]);

        Thread.sleep(5000);

        peers[1] = new PeerBuilderDHT(new PeerBuilder( new Number160( RND ) ).masterPeer( peers[0].peer() ).start()).start();
        ExampleUtils.bootstrap(peers);

        connections[1] = new P2PConnection("dummy" + String.valueOf(1), peers[1].peer(), peers[1]);
        connections[1].setShard(new Shard(0));
        channels[1] = p2PBroadcastService.createChannel(connections[1], P2PBroadcastChannelName.BLOCK);

        p2PBroadcastService.subscribeToChannel(channels[1]);

        HashSet<PeerAddress> peersSubscribed = p2PBroadcastService.getPeersOnChannel(channels[0]);
        TestCase.assertEquals(2, peersSubscribed.size());

        for (PeerDHT peer: peers) {
            peer.shutdown();
        }
    }

}
