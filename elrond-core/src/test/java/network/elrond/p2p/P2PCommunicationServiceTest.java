package network.elrond.p2p;

import junit.framework.TestCase;
import network.elrond.data.Transaction;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Test;

import java.net.ServerSocket;

public class P2PCommunicationServiceTest {

    @Test(expected = Exception.class)
    public void testAddressNullShouldThrowException() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse(null, 3);
        Assert.fail();
    }

    @Test(expected = Exception.class)
    public void testAddressInvalid1ShouldThrowException() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("aaa", 3);
        Assert.fail();
    }

    @Test(expected = Exception.class)
    public void testAddressInvalid2ShouldThrowException() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("255.0.0.0", 3);
        Assert.fail();
    }

    @Test(expected = Exception.class)
    public void testPortOutsideRangeShouldThrowException() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 65536);
        Assert.fail();
    }

    @Test
    public void testPingLocalHostNotServerRunning() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 4000);

        System.out.println(pingResponse.toString());

        TestCase.assertEquals(true, pingResponse.isReachablePing());
        TestCase.assertTrue((pingResponse.getReponseTimeMs() > 0) && (pingResponse.getReponseTimeMs() < 100));
        TestCase.assertEquals(false, pingResponse.isReachablePort());
    }

    @Test
    public void testPingWrongIP() throws Exception{
        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("1.2.3.4", 4000);

        System.out.println(pingResponse.toString());

        TestCase.assertEquals(false, pingResponse.isReachablePing());
        TestCase.assertTrue(pingResponse.getReponseTimeMs() == 0);
        TestCase.assertEquals(false, pingResponse.isReachablePort());
    }

    @Test
    public void testPingLocalHostServerIsRunning() throws Exception{
        ServerSocket serverSocket = new ServerSocket(4000);

        Thread thrAccespt = new Thread(() -> {
            try {
                serverSocket.accept();
            } catch(Exception ex){
                ex.printStackTrace();
            }
        });



        PingResponse pingResponse = AppServiceProvider.getP2PCommunicationService().getPingResponse("127.0.0.1", 4000);

        System.out.println(pingResponse.toString());

        TestCase.assertEquals(true, pingResponse.isReachablePing());
        TestCase.assertTrue((pingResponse.getReponseTimeMs() > 0) && (pingResponse.getReponseTimeMs() < 100));
        TestCase.assertEquals(true, pingResponse.isReachablePort());

        serverSocket.close();
    }
}
