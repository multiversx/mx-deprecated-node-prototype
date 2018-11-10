package network.elrond.p2p;

import junit.framework.TestCase;
import net.tomp2p.dht.PeerDHT;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.junit.Test;

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
}
