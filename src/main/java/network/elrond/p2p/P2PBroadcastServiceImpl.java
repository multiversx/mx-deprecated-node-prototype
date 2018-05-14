package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
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

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;

public class P2PBroadcastServiceImpl implements P2PBroadcastService {

    private static P2PBroadcastService instance = new P2PBroadcastServiceImpl();

    public static P2PBroadcastService instance() {
        return instance;
    }


    public P2PBroadcastConnection createConnection(AppContext context) throws IOException {

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

        return new P2PBroadcastConnection(peerId, peer, dht);

    }

    public P2PBroadcastChanel createChannel(P2PBroadcastConnection connection, String channelName) {

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
            peer.objectDataReply((sender, request) -> {
                for (P2PChannelListener listener : channel.getListeners()) {
                    listener.onReciveMessage(sender, request);
                }
                return null;
            });

            return channel;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean subscribeToChannel(P2PBroadcastChanel chanel) {
        try {


            P2PBroadcastConnection connection = chanel.getConnection();
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

            P2PBroadcastConnection connection = chanel.getConnection();
            String channelName = chanel.getName();

            PeerDHT dht = connection.getDht();

            FutureGet futureGet = dht.get(Number160.createHash(channelName)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet<PeerAddress> peersOnChannel;
                peersOnChannel = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                for (PeerAddress peer : peersOnChannel) {
                    FutureDirect futureDirect = dht.peer().sendDirect(peer).object(object).start();
                    futureDirect.awaitUninterruptibly();
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean unsubscribeFromChannel(P2PBroadcastChanel chanel) {
        try {

            P2PBroadcastConnection connection = chanel.getConnection();
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

        P2PBroadcastConnection connection = channels.get(0).getConnection();

        for (P2PBroadcastChanel chanel : channels) {
            unsubscribeFromChannel(chanel);
        }

        PeerDHT dht = connection.getDht();
        dht.peer().announceShutdown().start().awaitUninterruptibly();

        return true;
    }
}