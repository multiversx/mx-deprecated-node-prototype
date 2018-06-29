package network.elrond.p2p;

import network.elrond.application.AppContext;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class P2PBroadcastIT {


    @Test
    public void testBroadcastClientToServer() throws Exception {


        final AtomicInteger messagesCount = new AtomicInteger();
        messagesCount.set(0);

        P2PConnection server = createServer(5000);
        P2PBroadcastChanel serverChannel = createChannel(server);
        P2PChannelListener p2PChannelListener = (sender, request) -> {
            System.out.println(sender + "-" + sender.toString());
            if ((request.getPayload().equals("###"))) {
                messagesCount.incrementAndGet();
            }
        };

        serverChannel.getListeners().add(p2PChannelListener);


        List<P2PBroadcastChanel> clientChannels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            P2PConnection client = createClient(5000, 5001 + i);
            P2PBroadcastChanel clientChannel = createChannel(client);
            clientChannel.getListeners().add(p2PChannelListener);
            clientChannels.add(clientChannel);
        }


        AppServiceProvider.getP2PBroadcastService().publishToChannel(clientChannels.get(0), "###");
        Thread.sleep(1000);
        Assert.assertEquals(11, messagesCount.get());


    }

    @Test
    public void testBroadcastServerToClient() throws Exception {


        final AtomicInteger messagesCount = new AtomicInteger();
        messagesCount.set(0);

        P2PConnection server = createServer(4000);
        P2PBroadcastChanel serverChannel = createChannel(server);
        P2PChannelListener p2PChannelListener = (sender, request) -> {
            System.out.println(sender + "-" + sender.toString());
            if ((request.getPayload().equals("###"))) {
                messagesCount.incrementAndGet();
            }
        };

        serverChannel.getListeners().add(p2PChannelListener);


        List<P2PBroadcastChanel> clientChannels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            P2PConnection client = createClient(4000, 4001 + i);
            P2PBroadcastChanel clientChannel = createChannel(client);
            clientChannel.getListeners().add(p2PChannelListener);
            clientChannels.add(clientChannel);
        }

        AppServiceProvider.getP2PBroadcastService().publishToChannel(serverChannel, "###");
        Thread.sleep(1000);
        Assert.assertEquals(11, messagesCount.get());

        server.getPeer().shutdown().await();


    }


    private P2PBroadcastChanel createChannel(P2PConnection connection) {
        P2PBroadcastChanel chanel = AppServiceProvider.getP2PBroadcastService().createChannel(connection, P2PChannelName.TRANSACTION);
        AppServiceProvider.getP2PBroadcastService().subscribeToChannel(chanel);
        return chanel;
    }

//    private P2PConnection createClient(Integer masterPeerPort, Integer port) throws IOException {
//        AppContext context = new AppContext();
//        context.setMasterPeerIpAddress("127.0.0.1");
//        context.setMasterPeerPort(masterPeerPort);
//        context.setPort(port);
//        context.setNodeName("Client-" + port);
//
//        return AppServiceProvider.getP2PBroadcastService().createConnection(context);
//    }
//
//    private P2PConnection createServer(int port) throws IOException {
//        AppContext context = new AppContext();
//        context.setMasterPeerIpAddress("127.0.0.1");
//        context.setMasterPeerPort(port);
//        context.setPort(port);
//        context.setNodeName("Server");
//
//        return AppServiceProvider.getP2PBroadcastService().createConnection(context);
//    }

}
