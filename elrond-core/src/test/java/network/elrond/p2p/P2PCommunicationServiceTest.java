package network.elrond.p2p;

import junit.framework.TestCase;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.net.ServerSocket;

public class P2PCommunicationServiceTest {

    @Test (expected = IllegalArgumentException.class)
    public void testAddressNullShouldThrowException() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse(null, 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddressInvalid1ShouldThrowExceptio() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("aaa", 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddressInvalid2ShouldThrowExceptio() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("255.0.0.0", 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testPortOutsideRangeShouldThrowExceptio() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 65536);
    }

    @Test (expected = Exception.class)
    public void testPingLocalHostNotServerRunning() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 60000);

//        System.out.println(pingResponse.toString());
//
//        TestCase.assertEquals(true, pingResponse.isReachablePing());
//        TestCase.assertTrue((pingResponse.getReponseTimeMs() >= 0) && (pingResponse.getReponseTimeMs() < 1000));
//        TestCase.assertEquals(false, pingResponse.isReachablePort());
//        TestCase.assertFalse(pingResponse.getErrorMessage().equals(""));
    }

    @Test (expected = Exception.class)
    public void testPingWrongIP() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("1.2.3.4", 4000);
//
//        System.out.println(pingResponse.toString());
//
//        TestCase.assertEquals(false, pingResponse.isReachablePing());
//        TestCase.assertTrue(pingResponse.getReponseTimeMs() == 0);
//        TestCase.assertEquals(false, pingResponse.isReachablePort());
//        TestCase.assertFalse(pingResponse.getErrorMessage().equals(""));
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

        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 60000);

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
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("192.168.11.121", 4000);

        System.out.println(pingResponse.toString());

    }
}
