package network.elrond.p2p;

import net.tomp2p.peers.PeerAddress;
import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.crypto.PrivateKey;
import network.elrond.processor.AppTasks;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class P2PAppBroadcastTest {


    @Test
    public void testBroadcastSimple() throws Exception {

        final AtomicInteger messagesCount = new AtomicInteger();
        messagesCount.set(0);

        AppContext context = createServer(6000);
        context.setPrivateKey(new PrivateKey("seed"));
        Application app = new Application(context);
        AppTasks.INITIALIZE_PUBLIC_PRIVATE_KEYS.process(app);
        AppTasks.INIT_P2P_CONNECTION.process(app);
        AppTasks.INIT_SHARDING.process(app);
        app.getState().getConnection().setShard(app.getState().getShard());

        P2PBroadcastChannel channel = AppP2PManager.instance().subscribeToChannel(app, P2PBroadcastChannelName.TRANSACTION, new P2PChannelListener() {
            @Override
            public void onReceiveMessage(PeerAddress sender, P2PBroadcastMessage request) throws InterruptedException {
                if ((request.getPayload().equals("###"))) {
                    messagesCount.incrementAndGet();
                }
            }
        });
        AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, "###", 0);

        Thread.sleep(1000);
        Assert.assertEquals(1, messagesCount.get());
    }


    @Test
    public void testBroadcastThreadConsumer() throws Exception {


        final AtomicInteger messagesCount = new AtomicInteger();
        messagesCount.set(0);

        AppContext context = createServer(6000);
        context.setPrivateKey(new PrivateKey("seed"));
        Application app = new Application(context);
        AppTasks.INITIALIZE_PUBLIC_PRIVATE_KEYS.process(app);
        AppTasks.INIT_P2P_CONNECTION.process(app);
        AppTasks.INIT_SHARDING.process(app);
        app.getState().getConnection().setShard(app.getState().getShard());

        ArrayBlockingQueue<Object> queue = AppP2PManager.instance().subscribeToChannel(app, P2PBroadcastChannelName.TRANSACTION);

        Thread thread = new Thread(() -> {
            while (app.getState().isStillRunning()) {
                messagesCount.set(queue.size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        P2PBroadcastChannel channel = app.getState().getChannel(P2PBroadcastChannelName.TRANSACTION);
        AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, "###", 0);
        Thread.sleep(3000);
        Assert.assertEquals(1, messagesCount.get());
    }


    private AppContext createClient(Integer masterPeerPort, Integer port) throws IOException {
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(masterPeerPort);
        context.setPort(port);
        context.setNodeName("Client-" + port);

        return context;
    }

    private AppContext createServer(int port) throws IOException {
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(port);
        context.setPort(port);
        context.setNodeName("Server");

        return context;
    }


}
