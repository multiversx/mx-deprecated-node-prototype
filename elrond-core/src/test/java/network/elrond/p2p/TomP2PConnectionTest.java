package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureChannelCreator;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.message.Message;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.p2p.StructuredBroadcastHandler;
import net.tomp2p.p2p.builder.BroadcastBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMap;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.ConcurrentCacheMap;
import net.tomp2p.utils.Utils;
import org.apache.logging.log4j.LogManager;

import java.net.InetAddress;
import java.util.*;

public class TomP2PConnectionTest {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(TomP2PConnectionTest.class);

    //@Test
    public void testConnectionAndDataRetention() throws Exception{

        Peer peerA = new PeerBuilder(new Number160(1)).ports(7001).start();
        PeerDHT dhtA = new PeerBuilderDHT(peerA).start();

        Peer peerB = new PeerBuilder(new Number160(2)).ports(7002).start();
        PeerDHT dhtB = new PeerBuilderDHT(peerB).start();

        Peer peerC = new PeerBuilder(new Number160(3)).ports(7003).start();
        PeerDHT dhtC = new PeerBuilderDHT(peerA).start();

        InetAddress localhost = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});


        FutureBootstrap fb = peerB.bootstrap().inetAddress(localhost).ports(7001).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peerB.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        fb = peerC.bootstrap().inetAddress(localhost).ports(7001).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peerB.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        Number160 hash = Number160.createHash("A");

        FuturePut fp = dhtA.put(hash).data(new Data("A")).start().awaitUninterruptibly();

        Peer peerD = new PeerBuilder(new Number160(4)).ports(7004).start();
        PeerDHT dhtD = new PeerBuilderDHT(peerD).start();
        fb = peerD.bootstrap().inetAddress(localhost).ports(7001).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peerD.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        dhtA.shutdown().awaitUninterruptibly();
        dhtB.shutdown().awaitUninterruptibly();

        Peer peerE = new PeerBuilder(new Number160(5)).ports(7005).start();
        PeerDHT dhtE = new PeerBuilderDHT(peerE).start();
        fb = peerE.bootstrap().inetAddress(localhost).ports(7001).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peerE.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        FutureGet futureGet = dhtD.get(hash).start().awaitUninterruptibly();

        if (futureGet.data() == null){
            System.out.println("Data null");
        } else {
            Object obj = futureGet.data().object();
            if (obj == null) {
                System.out.println("Object null");
            } else {
                System.out.println(obj.toString());
            }
        }
    }


    //@Test
    public void testConnectionAndDataRetentionBig() throws Exception{
        List<Peer> peers = new ArrayList<>();
        List<PeerDHT> peerDHTS = new ArrayList<>();

        List<Object> lob = new ArrayList<>();

        Peer peerSeeder = new PeerBuilder(new Number160(99999999)).ports(6999).enableBroadcast(true).enableRouting(true).start();
        PeerDHT dhtSeeder = new PeerBuilderDHT(peerSeeder).start();

        peerSeeder.peerAddress().changeRelayed(true);

        InetAddress localhost = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});

        //lob.add(new AutoReplication(peerSeeder).start());

        int maxSize = 20;

        for (int i = 0; i < maxSize / 2; i++){

            Peer peer = new PeerBuilder(new Number160(i + 1)).ports(7000 + i).enableBroadcast(true).enableRouting(true).start();
            PeerDHT dht = new PeerBuilderDHT(peer).start();

            peer.peerAddress().changeRelayed(true);

//            FutureBootstrap fb = peer.bootstrap().inetAddress(localhost).ports(6999).start();
//            fb.awaitUninterruptibly();
//            if (fb.isSuccess()) {
//                peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
//            }

            peer.discover().inetAddress(localhost).ports(6999).start().awaitUninterruptibly();
            peer.bootstrap().inetAddress(localhost).ports(6999).start().awaitUninterruptibly();

            prettyPrint(peerSeeder, 6999, 6999 + maxSize);
            prettyPrint(peer, 6999, 6999 + maxSize);

            //lob.add(new AutoReplication(peer).start());

            peers.add(peer);
            peerDHTS.add(dht);
        }

        System.out.println("Done creating 1");

        for (int i = 0; i < 1000; i++){
            Number160 hash = Number160.createHash(i);
            FuturePut fp = dhtSeeder.put(hash).data(new Data(i)).start().awaitUninterruptibly();
        }

        System.out.println("Done put");

        for (int i = maxSize / 2; i < maxSize; i++){
            Peer peer = new PeerBuilder(new Number160(i + 1)).ports(7000 + i).enableBroadcast(true).enableRouting(true).start();
            PeerDHT dht = new PeerBuilderDHT(peer).start();

            peer.peerAddress().changeRelayed(true);

//            FutureBootstrap fb = peer.bootstrap().inetAddress(localhost).ports(6999).start();
//            fb.awaitUninterruptibly();
//            if (fb.isSuccess()) {
//                peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
//            }

            peer.discover().inetAddress(localhost).ports(6999).start().awaitUninterruptibly();
            peer.bootstrap().inetAddress(localhost).ports(6999).start().awaitUninterruptibly();

            prettyPrint(peerSeeder, 6999, 6999 + maxSize);
            prettyPrint(peer, 6999, 6999 + maxSize);


            //lob.add(new AutoReplication(peer).start());

            peers.add(peer);
            peerDHTS.add(dht);
        }

        prettyPrint(peerSeeder, 6999, 6999 + peers.size());
        for (int i = 0; i < peers.size(); i++){
            prettyPrint(peers.get(i), 6999, 6999 + peers.size());
        }

        System.out.println("Done creating 2");

        System.out.println("Check data on seeder...");
        for (int i = 0; i < 1000; i++){
            Number160 hash = Number160.createHash(i);

            FutureGet futureGet = dhtSeeder.get(hash).withDigest().start().awaitUninterruptibly();

            if (futureGet.data() == null){
                System.out.println("Data null on " + String.valueOf(i) + " & status: " + futureGet.isSuccess());
            } else {
                Object obj = futureGet.data().object();
                if (obj == null) {
                    System.out.println("Object null on " + String.valueOf(i) + " & status: " + futureGet.isSuccess());
                } else {
                }
            }
        }



        for (int j = 0; j < peerDHTS.size(); j++){
            System.out.println("Check data on " + peerDHTS.get(j).peerAddress().tcpPort() + "...");
            for (int i = 0; i < 1000; i++){
                Number160 hash = Number160.createHash(i);

                FutureGet futureGet = dhtSeeder.get(hash).withDigest().start().awaitUninterruptibly();

                if (futureGet.data() == null){
                    System.out.println("Data null on " + String.valueOf(i) + " & status: " + futureGet.isSuccess());
                } else {
                    Object obj = futureGet.data().object();
                    if (obj == null) {
                        System.out.println("Object null on " + String.valueOf(i) + " & status: " + futureGet.isSuccess());
                    } else {
                    }
                }
            }


        }

        System.out.println("Done");


    }

    //@Test
    public void testConnectionAndBroadcast() throws Exception{
        class MyStructuredBroadcastHandler extends StructuredBroadcastHandler {

            private final ConcurrentCacheMap<Number160, Boolean> cache = new ConcurrentCacheMap<Number160, Boolean>();
            private volatile Peer peer;

            public MyStructuredBroadcastHandler() {
                peer = null;
            }

            @Override
            public StructuredBroadcastHandler receive(Message message) {
                if (peer == null){
                    logger.warn("Peer not initialized!");
                    return this;
                }

                final PeerAddress sender = message.sender();

                final Number160 messageKey = message.key(0);
                if (twiceSeen(messageKey)){
                    //logger.debug("{} already received the message: {}", peer.peerAddress().tcpPort(), messageKey);
                    return this;
                }

                logger.info("{} received the message: {}", peer.peerAddress().tcpPort(), messageKey);

                //broadcast
                final int hopCount = message.intAt(0);

                final NavigableMap<Number640, Data> dataMap;
                if (message.dataMap(0) != null) {
                    dataMap = message.dataMap(0).dataMap();
                } else {
                    dataMap = null;
                }

                List<PeerAddress> listToSend = peer.peerBean().peerMap().all();
                //listToSend.addAll(peer.peerBean().peerMap().allOverflow());
                for (PeerAddress peerAddress : listToSend){
                    if (peerAddress == sender){
                        continue;
                    }

                    int bucketNr = PeerMap.classMember(peerAddress.peerId(),
                            peer.peerID());
                    doSend(messageKey, dataMap, hopCount, message.isUdp(), peerAddress,
                            bucketNr);
                }

                return this;
            }

            @Override
            public StructuredBroadcastHandler init(Peer peer) {
                this.peer = peer;
                return super.init(peer);
            }

            private boolean twiceSeen(final Number160 messageKey) {
                Boolean isInCache = cache.putIfAbsent(messageKey, Boolean.TRUE);
                if (isInCache != null) {
                    // ttl refresh
                    cache.put(messageKey, Boolean.TRUE);
                    return true;
                }
                return false;
            }

            private void doSend(final Number160 messageKey,
                                final NavigableMap<Number640, Data> dataMap, final int hopCounter,
                                final boolean isUDP, final PeerAddress peerAddress,
                                final int bucketNr) {

                FutureChannelCreator frr = peer.connectionBean().reservation()
                        .create(isUDP ? 1 : 0, isUDP ? 0 : 1);
                frr.addListener(new BaseFutureAdapter<FutureChannelCreator>() {
                    @Override
                    public void operationComplete(final FutureChannelCreator future)
                            throws Exception {
                        if (future.isSuccess()) {
                            BroadcastBuilder broadcastBuilder = new BroadcastBuilder(
                                    peer, messageKey);
                            broadcastBuilder.dataMap(dataMap);
                            broadcastBuilder.hopCounter(hopCounter + 1);
                            broadcastBuilder.udp(isUDP);
                            FutureResponse futureResponse = peer.broadcastRPC()
                                    .send(peerAddress, broadcastBuilder,
                                            future.channelCreator(), broadcastBuilder,
                                            bucketNr);
                            Utils.addReleaseListener(future.channelCreator(),
                                    futureResponse);
                        } else {
                            Utils.addReleaseListener(future.channelCreator());
                        }
                    }
                });
            }
        };


        List<Peer> peers = new ArrayList<>();
        //List<PeerDHT> peerDHTS = new ArrayList<>();

        Peer peerSeeder = new PeerBuilder(new Number160(99999999)).ports(6999).broadcastHandler(new MyStructuredBroadcastHandler()).
                start();

        InetAddress localhost = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});

        int maxSize = 20;

        for (int i = 0; i < maxSize; i++){
            PeerAddress peerAddressSeeder = new PeerAddress(Number160.createHash(UUID.randomUUID().toString()), localhost, 6999, 6999);

            Peer peer = new PeerBuilder(Number160.createHash(UUID.randomUUID().toString())).ports(7000 + i).
                    broadcastHandler(new MyStructuredBroadcastHandler()).start();

            peer.bootstrap().inetAddress(localhost).ports(6999).
                    start().awaitUninterruptibly();
            peer.discover().inetAddress(localhost).ports(6999).start().awaitUninterruptibly();

            prettyPrint(peerSeeder, 6999, 6999 + maxSize);
            prettyPrint(peer, 6999, 6999 + maxSize);

            peers.add(peer);
        }

        logger.fatal("Done creating peers:");

        prettyPrint(peerSeeder, 6999, 6999 + peers.size());
        for (int i = 0; i < peers.size(); i++){
            prettyPrint(peers.get(i), 6999, 6999 + peers.size());
        }

        NavigableMap<Number640, Data> dataMap = new TreeMap<>();
        dataMap.put(Number640.ZERO, new Data("testme"));

        //the message key *must* be unique for broadcast you do. In order to avoid duplicates, multiple
        //messages with the same message key will be ignored, thus, subsequent broadcast may fail.
        Number160 messageKey = Number160.createHash("blub");
        //take any peer and send broadcast
        peers.get(19).broadcast(messageKey).dataMap(dataMap).start();

        Thread.sleep(2000);

    }

    //@Test
    public void testKnowEverybody() throws Exception{
        class MyObjectDataReply implements ObjectDataReply {

            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                logger.info("Peer {} got message: {}", sender.tcpPort(), request.toString());

                return(null);
            }
        }

        List<Peer> peers = new ArrayList<>();

        InetAddress localhost = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});


        int maxSize = 20;

        for ( int i = 0; i < maxSize; i++ ) {
            peers.add(new PeerBuilder( new Number160(7000 + i)).ports(7000 + i).start());
            peers.get(i).objectDataReply(new MyObjectDataReply());
        }

        logger.info("Done creating peers...");

//        for (int i = 0; i < peers.size(); i++){
//            for (int j = 0; j < peers.size(); j++){
//                if (i == j){
//                    continue;
//                }
//
//                peers.get(i).peerBean().peerMap().peerFound(peers.get(j).peerAddress(), null, null, null);
//            }
//        }

        //Thread.sleep(10000);

        logger.info("Done connecting...");

        for (int i = 0; i < peers.size(); i++){
            prettyPrint(peers.get(i), 7000, 7000 + peers.size());
        }

        for (int i = 0; i < peers.size();i++){
            peers.get(2).sendDirect(peers.get(i).peerAddress()).object("From 2 to " + String.valueOf(i)).start().awaitUninterruptibly();
        }


    }



    private void prettyPrint(Peer peer, int startPort, int endPort){
        List<PeerAddress> list = peer.peerBean().peerMap().all();
        //list.addAll(peer.peerBean().peerMap().allOverflow());

        //List<PeerAddress> list = peer.peerBean().peerMap().fromEachBag(3, Number160.BITS);


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Node ");
        stringBuilder.append(peer.peerAddress().tcpPort());
        stringBuilder.append(" has ");
        stringBuilder.append(String.format("%3d", list.size()));
        stringBuilder.append(" connections, ");
        stringBuilder.append(String.format("%3d", peer.peerBean().peerMap().nrFilledBags()));
        stringBuilder.append(" filled bags, ");
        stringBuilder.append(peer.peerAddress().isRelayed());
        stringBuilder.append(" relayed >>> ");

        List<Integer> ports = new ArrayList<>();

        for (PeerAddress peerAddress:list){
            ports.add(peerAddress.tcpPort());
        }

        for (int port = startPort; port <= endPort; port++){
            if (ports.contains(port)){
                stringBuilder.append(" ");
                stringBuilder.append(port);
                stringBuilder.append(" ");
            } else {
                stringBuilder.append("      ");
            }
        }

        logger.info(stringBuilder.toString());
    }



}
