package network.elrond.p2p;

import junit.framework.TestCase;
import net.tomp2p.dht.FuturePut;
import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.p2p.P2PBroadcastServiceImpl;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.util.Random;

public class P2PBroadcastServiceImplTest {
    @Test
    public void testContains() throws Exception{
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001);
        context.setPeerId(0);


        context.setEmitter(true);
        Application app = new Application(context);
        app.start();

        AppState state = app.getState();

        Random rdm = new Random(10);

        AppP2PManager.instance().subscribeToChannel(app, "TEST", (sender, request) -> {
        });


        P2PBroadcastChanel channel = state.getChanel("TEST");

        FuturePut fp = AppServiceProvider.getP2PObjectService().put(channel.getConnection(), "TESTAAAA",
               "TESTAAAA_DATA");

        TestCase.assertEquals(true, fp.isSuccess());



    }

}
