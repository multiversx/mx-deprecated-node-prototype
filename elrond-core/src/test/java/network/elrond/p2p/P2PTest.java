package network.elrond.p2p;

import junit.framework.TestCase;
import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.crypto.PrivateKey;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.util.Scanner;

public class P2PTest {

    @Test
    public void testOverride1() throws Exception {
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001 /*+ new Random().nextInt(10000)*/);
        context.setNodeName("AAA");
        context.setPrivateKey(new PrivateKey("test"));

        Application app = new Application(context);
        app.start();

        AppState state = app.getState();

        P2PObjectService p2PObjectService = AppServiceProvider.getP2PObjectService();

        p2PObjectService.putJsonEncoded("string1", "AAAA", state.getConnection());

        String strGet1 = p2PObjectService.get(state.getConnection(), "AAAA").toString();

        TestCase.assertEquals("\"string1\"", strGet1);

        p2PObjectService.putJsonEncoded("string2", "AAAA", state.getConnection());

        String strGet2 = p2PObjectService.get(state.getConnection(), "AAAA").toString();
        TestCase.assertEquals("\"string2\"", strGet2);

        app.stop();
    }

    public void testOverrideWriter() throws Exception {
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setNodeName("BBB");
        context.setStorageBasePath("producer");


        Application app = new Application(context);
        app.start();

        AppState state = app.getState();

        P2PObjectService p2PObjectService = AppServiceProvider.getP2PObjectService();

        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (state.isStillRunning()) {

            p2PObjectService.putJsonEncoded("string1", "AAAA", state.getConnection());

            Thread.sleep(1000);

            p2PObjectService.putJsonEncoded("string2", "AAAA", state.getConnection());

            Thread.sleep(1000);
        }
    }

    public void testOverrideReader() throws Exception {
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001 /*+ new Random().nextInt(10000)*/);
        context.setNodeName("AAA");
        context.setStorageBasePath("consumer");

        Application app = new Application(context);
        app.start();

        AppState state = app.getState();

        P2PObjectService p2PObjectService = AppServiceProvider.getP2PObjectService();

        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (state.isStillRunning()) {

            String strGet = p2PObjectService.get(state.getConnection(), "AAAA").toString();
            System.out.println(strGet);
            Thread.sleep(100);
        }
    }
}
