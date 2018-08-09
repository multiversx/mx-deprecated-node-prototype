package network.elrond.p2p;

import net.tomp2p.dht.*;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.DigestResult;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.Map;

/**
 * Example of indirect replication with put.
 *
 * @author Thomas Bocek
 *
 */
public final class ExampleIndirectReplication {

    private static final int ONE_SECOND = 1000;

    /**
     * Empty constructor.
     */
    private ExampleIndirectReplication() {
    }

    /**
     * Create 3 peers and start the example.
     *
     * @param args
     *            Empty
     * @throws Exception .
     */
    public static void main(final String[] args) throws Exception {
        exampleIndirectReplication();
    }

    /**
     * Example of indirect replication with put. We store data in the DHT, then peers join that are closer to this data.
     * The indirect replication moves the content to the close peers.
     *
     * @throws IOException .
     * @throws InterruptedException .
     */
    private static void exampleIndirectReplication() throws IOException, InterruptedException, ClassNotFoundException {
        int puts = 100;
        PeerDHT[] peers = new PeerDHT[100];
        final int port1 = 4001;
        final int nr1 = 1;
        for(int i = 1;i<100;i++){
            PeerDHT peer = new PeerBuilderDHT(new PeerBuilder(new Number160(nr1+i)).ports(port1 + i).start()).start();
            peers[i] = peer;
            //new IndirectReplication(peer).start();
        }

        PeerDHT peer1 = new PeerBuilderDHT(new PeerBuilder(new Number160(nr1)).ports(port1).start()).start();
        peers[0] = peer1;
        IndirectReplication ind = new IndirectReplication(peer1);
        ind.replicationFactor(100);
        ind.start();


        for(int i = 0; i<puts;i++){
            FuturePut futurePut = peer1.put(new Number160(nr1 + i)).data(new Data("store on peer1 " + i)).start();
            futurePut.awaitUninterruptibly();
        }


        FutureDigest futureDigest = peer1.digest(new Number160(nr1)).start();
        futureDigest.awaitUninterruptibly();
        System.out.println("we found the data on:");
        for(Map.Entry<PeerAddress, DigestResult> entry: futureDigest.rawDigest().entrySet()) {
            System.out.println("peer " + entry.getKey()+" reported " +entry.getValue().keyDigest().size());
        }

        for(int i = 1;i<50;i++) {
            // now peer1 gets to know peer2 and peer3, transfer the data
            peer1.peer().bootstrap().peerAddress(peers[i].peerAddress()).start().awaitUninterruptibly();
//            Thread.sleep(100);
//            FutureGet get = peers[i].get(new Number160(nr1)).start();
//            get.awaitUninterruptibly();
//            String message = "" + i;
//            if(get.dataMap().values().iterator().hasNext()) {
//                message += get.dataMap().values().iterator().next().object();
//            }
//            System.out.println(message);
        }


        Thread.sleep(5000);
        futureDigest = peer1.digest(new Number160(nr1)).start();
        futureDigest.awaitUninterruptibly();
        System.out.println("we found the data on:");
        for(Map.Entry<PeerAddress, DigestResult> entry: futureDigest.rawDigest().entrySet()) {
            System.out.println("peer " + entry.getKey()+" reported " +entry.getValue().keyDigest().size());
        }

        for(int i = 0; i<puts;i++){
            FuturePut futurePut = peer1.put(new Number160(nr1 + i)).data(new Data(new int[100*100])).start();
            futurePut.awaitUninterruptibly();
        }

//        FuturePut futurePut2 = peers[0].put(new Number160(nr1)).data(new Data("store on all peers")).start();
//        futurePut2.awaitUninterruptibly();
        for(int i = 50;i<100;i++) {
            peer1.peer().bootstrap().peerAddress(peers[i].peerAddress()).start().awaitUninterruptibly();
        }
        int howMany = 0;
        for(int i = 0;i<puts;i++) {
//            futureDigest = peer1.digest(new Number160(nr1+i)).start();
//            futureDigest.awaitUninterruptibly();


            for(PeerDHT peer : peers){
                FutureGet get = peer.get(new Number160(nr1+i)).start();
                get.awaitUninterruptibly();
                String message = "";
                if(get.dataMap().values().iterator().hasNext()) {
                    message += get.dataMap().values().iterator().next().object();
                    howMany++;
                }
               // System.out.println(peer.peerID() + " - " + message);

            }
            System.out.println("we found the data on:" + howMany);

//            for (Map.Entry<PeerAddress, DigestResult> entry : futureDigest.rawDigest().entrySet()) {
//                System.out.println("peer " + entry.getKey() + " reported " + entry.getValue().keyDigest().size());
//                howMany ++;
//            }
        }

        howMany = 0;
        for(int i = 0;i<puts;i++) {
//            futureDigest = peer1.digest(new Number160(nr1+i)).start();
//            futureDigest.awaitUninterruptibly();


            for(PeerDHT peer : peers){
                FutureGet get = peer.get(new Number160(nr1+i)).start();
                get.awaitUninterruptibly();
                String message = "";
                if(get.dataMap().values().iterator().hasNext()) {
                    message += get.dataMap().values().iterator().next().object();
                    howMany++;
                }
                // System.out.println(peer.peerID() + " - " + message);

            }
            System.out.println("we found the data on:" + howMany);

//            for (Map.Entry<PeerAddress, DigestResult> entry : futureDigest.rawDigest().entrySet()) {
//                System.out.println("peer " + entry.getKey() + " reported " + entry.getValue().keyDigest().size());
//                howMany ++;
//            }
        }

        System.out.println("Howmany = " + howMany);
        FuturePut futurePut3 = peers[2].put(new Number160(nr1)).data(new Data("store on 2")).start();
        futurePut3.awaitUninterruptibly();
        int count = 0;
        for(PeerDHT peer : peers){
            FutureGet get = peer.get(new Number160(nr1)).start();
            get.awaitUninterruptibly();
            String message = "" + count++;
            if(get.dataMap().values().iterator().hasNext()) {
                message += get.dataMap().values().iterator().next().object();
            }
            System.out.println(peer.peerID() + " - " +message);

        }
        FuturePut futurePut4 = peers[10].put(new Number160(nr1)).data(new Data("store on 10")).start();
        futurePut4.awaitUninterruptibly();

        count = 0;
        for(PeerDHT peer : peers){
            FutureGet get = peer.get(new Number160(nr1)).start();
            get.awaitUninterruptibly();
            String message = "" + count++;
            if(get.dataMap().values().iterator().hasNext()) {
                 message += get.dataMap().values().iterator().next().object();
            }
            System.out.println(peer.peerID() + " - " + message);

        }

        //the result shows in the command line 1, 1, 1. This behavior is explaind as follows:
        //
        //The bootstrap from peer1 to peer2 causes peer 1 to replicate the data object to peer2
        //The bootstrap from peer1 to peer3 causes the peer3 to become the new responsible peer. Thus, peer1
        //transfers the data to peer3 and all three peers have the data.
        //
        //Now consider the following scenario, if we change the order of the bootstrap, so that peer1 first
        //bootstraps to peer3 and then to peer2, we will see 1, 0, 1 in the command line. This behavior is explained
        //as follows:
        //
        //First peer1 will figure out that peer3 is responsible and transfer the data to this peer. Then peer1 bootstraps to
        //peer2. However, as peer1 is not responsible anymore and peer3 does not know yet peer2, peer 2 won't see that data
        //until peer3 does the periodical replication check.
        shutdown(peers);
    }

    /**
     * Shutdown all the peers.
     *
     * @param peers
     *            The peers in this P2P network
     */
    private static void shutdown(final PeerDHT[] peers) {
        for (PeerDHT peer : peers) {
            peer.shutdown();
        }
    }
}