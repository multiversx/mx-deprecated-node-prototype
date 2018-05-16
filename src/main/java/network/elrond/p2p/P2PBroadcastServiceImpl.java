package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import network.elrond.application.AppContext;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;

public class P2PBroadcastServiceImpl implements P2PBroadcastService {


    public P2PConnection createConnection(AppContext context) throws IOException {

        Integer peerId = context.getPeerId();
        int peerPort = context.getPort();
        String masterPeerIpAddress = context.getMasterPeerIpAddress();
        int masterPeerPort = context.getMasterPeerPort();

        Peer peer = new PeerBuilder(Number160.createHash(peerId)).ports(peerPort).start();
        PeerDHT dht = new PeerBuilderDHT(peer).start();


        FutureBootstrap fb = peer
                .bootstrap()
                .inetAddress(InetAddress.getByName(masterPeerIpAddress))
                .ports(masterPeerPort).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        return new P2PConnection(peerId, peer, dht);

    }

    public P2PBroadcastChanel createChannel(P2PConnection connection, String channelName) {

        try {

            PeerDHT dht = connection.getDht();

            FutureGet futureGet = dht.get(Number160.createHash(channelName)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess() && futureGet.isEmpty())
                dht.put(Number160.createHash(channelName))
                        .data(new Data(new HashSet<PeerAddress>()))
                        .start()
                        .awaitUninterruptibly();
            P2PBroadcastChanel channel = new P2PBroadcastChanel(channelName, connection);

            Peer peer = connection.getPeer();
            peer.objectDataReply(connection.registerChannel(channel));

            return channel;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean subscribeToChannel(P2PBroadcastChanel chanel) {
        try {


            P2PConnection connection = chanel.getConnection();
            String channelName = chanel.getName();
            PeerDHT dht = connection.getDht();

            FutureGet futureGet = dht.get(Number160.createHash(channelName)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) return false;
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                peersOnChannel.add(dht.peer().peerAddress());
                dht.put(Number160.createHash(channelName)).data(new Data(peersOnChannel)).start().awaitUninterruptibly();

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean publishToChannel(P2PBroadcastChanel chanel, Object object) {
        try {

            P2PConnection connection = chanel.getConnection();
            String channelName = chanel.getName();

            PeerDHT dht = connection.getDht();

            FutureGet futureGet = dht.get(Number160.createHash(channelName)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                for (PeerAddress peer : peersOnChannel) {
                    FutureDirect futureDirect = dht.peer()
                            .sendDirect(peer)
                            .object(new P2PBroadcastMessage(channelName, object))
                            .start();
                    futureDirect.awaitUninterruptibly();
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object get(P2PBroadcastChanel chanel, String key) throws ClassNotFoundException, IOException {
        return AppServiceProvider.getP2PObjectService().get(chanel.getConnection(), key);
    }

    public FuturePut put(P2PBroadcastChanel chanel, String key, Object value) throws IOException {
        return AppServiceProvider.getP2PObjectService().put(chanel.getConnection(), key, value);
    }


    @SuppressWarnings("unchecked")
    public boolean unsubscribeFromChannel(P2PBroadcastChanel chanel) {
        try {

            P2PConnection connection = chanel.getConnection();
            String channelName = chanel.getName();

            PeerDHT dht = connection.getDht();

            FutureGet futureGet = dht.get(Number160.createHash(channelName)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) return false;
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                peersOnChannel.remove(dht.peer().peerAddress());
                dht.put(Number160.createHash(channelName)).data(new Data(peersOnChannel)).start().awaitUninterruptibly();

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean leaveNetwork(List<P2PBroadcastChanel> channels) {

        P2PConnection connection = channels.get(0).getConnection();

        for (P2PBroadcastChanel chanel : channels) {
            unsubscribeFromChannel(chanel);
        }

        PeerDHT dht = connection.getDht();
        dht.peer().announceShutdown().start().awaitUninterruptibly();

        return true;
    }
}