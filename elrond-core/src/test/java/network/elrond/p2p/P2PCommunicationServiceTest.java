package network.elrond.p2p;

import junit.framework.TestCase;
import network.elrond.p2p.model.PingResponse;
import network.elrond.p2p.service.P2PCommunicationService;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.net.ServerSocket;

public class P2PCommunicationServiceTest {

    @Test (expected = IllegalArgumentException.class)
    public void testAddressNullShouldThrowException() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse(null, 3, true);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddressInvalid1ShouldThrowExceptio() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("aaa", 3, true);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddressInvalid2ShouldThrowExceptio() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("255.0.0.0", 3, true);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testPortOutsideRangeShouldThrowExceptio() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 65536, true);
    }

    @Test (expected = Exception.class)
    public void testPingLocalHostNotServerRunning() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 60000, true);
    }

    @Test
    public void testPingLocalHostNotServerRunningNotThrowing() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 60000, false);

        TestCase.assertTrue(pingResponse.isReachablePing());
        TestCase.assertFalse(pingResponse.isReachablePort());
    }

    @Test (expected = Exception.class)
    public void testPingWrongIP() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("1.2.3.4", 4000, true);
    }

    @Test
    public void testPingLocalHostServerIsRunning() throws Exception{
        ServerSocket serverSocket = new ServerSocket(60000);

        Thread thrAccept = new Thread(() -> {
            try {
                serverSocket.accept();
            } catch(Exception ex){
                ex.printStackTrace();
            }
        });
        thrAccept.start();

        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 60000, true);

        System.out.println(pingResponse.toString());

        TestCase.assertEquals(true, pingResponse.isReachablePing());
        TestCase.assertTrue((pingResponse.getReponseTimeMs() >= 0) && (pingResponse.getReponseTimeMs() < 1000));
        TestCase.assertEquals(true, pingResponse.isReachablePort());

        serverSocket.close();
        thrAccept.join();
    }

    //@Test
    public void testIsPortReachable(){
        P2PCommunicationService p2PCommunicationService = AppServiceProvider.getP2PCommunicationService();

        System.out.println(p2PCommunicationService.isPortReachable("192.168.11.101", 445, 500));


    }

    //@Test
    public void testPingIP() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("35.156.49.216", 22, true);

        System.out.println(pingResponse.toString());
    }


}
