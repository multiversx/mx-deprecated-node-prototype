package network.elrond.p2p;

import junit.framework.TestCase;
import network.elrond.data.Transaction;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.ServerSocket;

public class P2PCommunicationServiceTest {

    @Test
    public void testAddressNullShouldReturnError() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse(null, 3);
        TestCase.assertEquals("address is null", pingResponse.getErrorMessage());
    }

    @Test
    public void testAddressInvalid1ShouldReturnError() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("aaa", 3);
        TestCase.assertEquals("address is not valid", pingResponse.getErrorMessage());
    }

    @Test
    public void testAddressInvalid2ShouldReturnError() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("255.0.0.0", 3);
        TestCase.assertEquals("address is not valid", pingResponse.getErrorMessage());
    }

    @Test
    public void testPortOutsideRangeShouldReturnError() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 65536);
        TestCase.assertEquals("port not valid", pingResponse.getErrorMessage());
    }

    @Test
    public void testPingLocalHostNotServerRunning() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 60000);

        System.out.println(pingResponse.toString());

        TestCase.assertEquals(true, pingResponse.isReachablePing());
        TestCase.assertTrue((pingResponse.getReponseTimeMs() >= 0) && (pingResponse.getReponseTimeMs() < 1000));
        TestCase.assertEquals(false, pingResponse.isReachablePort());
        TestCase.assertFalse(pingResponse.getErrorMessage().equals(""));
    }

    @Test
    public void testPingWrongIP() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("1.2.3.4", 4000);

        System.out.println(pingResponse.toString());

        TestCase.assertEquals(false, pingResponse.isReachablePing());
        TestCase.assertTrue(pingResponse.getReponseTimeMs() == 0);
        TestCase.assertEquals(false, pingResponse.isReachablePort());
        TestCase.assertFalse(pingResponse.getErrorMessage().equals(""));
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
        TestCase.assertEquals("", pingResponse.getErrorMessage());

        serverSocket.close();
        thrAccept.join();
    }

    @Test
    public void testPingIP() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("192.168.11.121", 4000);

        System.out.println(pingResponse.toString());

    }
}
