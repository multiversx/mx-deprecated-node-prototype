package network.elrond.p2p;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;

import java.io.IOException;

/**
 * See http://tomp2p.net/doc/quick/ for more information
 */
public class ExampleBootstrap {
    private static final int PORT = 4001;

    /**
     * Starts the boostrap example.
     *
     * @param args
     *            No arguments needed
     * @throws IOException
     * @throws InterruptedException
     * @throws Exception
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Peer[] peers = null;
        try {
            peers = ExampleUtils.createAndAttachNodes(3, PORT);

            for (int i = 0; i < peers.length; i++) {
                System.out.println("peer[" + i + "]: " + peers[i].peerAddress());
            }

            FutureBootstrap futureBootstrap1 = peers[1].bootstrap().peerAddress(peers[0].peerAddress()).start();
            futureBootstrap1.awaitUninterruptibly();
            System.out.println("peer[0] knows: " + peers[0].peerBean().peerMap().all() + " unverified: "
                    + peers[0].peerBean().peerMap().allOverflow());
            System.out.println("wait for maintenance ping");
            Thread.sleep(2000);
            System.out.println("peer[0] knows: " + peers[0].peerBean().peerMap().all() + " unverified: "
                    + peers[0].peerBean().peerMap().allOverflow());
            FutureBootstrap futureBootstrap2 = peers[2].bootstrap().peerAddress(peers[0].peerAddress()).start();
            futureBootstrap2.awaitUninterruptibly();
            // list all the peers C knows by now:
            System.out.println("peer[2] knows: " + peers[2].peerBean().peerMap().all());
            System.out.println("peer[2] knows: " + peers[2].peerBean().peerMap().all() + " unverified: "
                    + peers[2].peerBean().peerMap().allOverflow());

        } finally {
            // 0 is the master
            if (peers != null && peers[0] != null) {
                peers[0].shutdown();
            }
        }
    }
}