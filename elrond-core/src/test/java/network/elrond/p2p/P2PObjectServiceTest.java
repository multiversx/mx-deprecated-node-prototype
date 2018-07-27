package network.elrond.p2p;

import junit.framework.TestCase;
import net.tomp2p.dht.*;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import network.elrond.ContextCreator;
import network.elrond.application.AppContext;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.BootstrapType;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class P2PObjectServiceTest {

    @Test
    public void testPutIfAbsentOnDHT() throws Exception {
        String nodeName = "elrond-seeder";

        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey("elrond-node-1").getValue());

        AppContext appContext = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);

        P2PConnection connection = AppServiceProvider.getP2PConnectionService().createConnection(appContext);

        PeerDHT peerDHT = connection.getDht();

        //put aaa->bbb
        PutBuilder putBuilder = peerDHT.put(new Number160(1234));
        putBuilder.data(new Data("bbb"));
        FuturePut futurePut1 = putBuilder.start().awaitUninterruptibly();
        putBuilder.start().awaitUninterruptibly();

        //test aaa->bbb
        GetBuilder getBuilder = peerDHT.get(new Number160(1234));
        FutureGet futureGet = getBuilder.start().awaitUninterruptibly();
        TestCase.assertNotNull(futureGet.data());
        TestCase.assertEquals("bbb", futureGet.data().object().toString());

        //try put aaa->ccc
        putBuilder = peerDHT.put(new Number160(1234));
        putBuilder.data(new Data("ccc"));
        putBuilder.putIfAbsent();

        FuturePut futurePut2 = putBuilder.start().awaitUninterruptibly();

        //test aaa->bbb
        getBuilder = peerDHT.get(new Number160(1234));
        futureGet = getBuilder.start().awaitUninterruptibly();
        TestCase.assertNotNull(futureGet.data());
        TestCase.assertEquals("bbb", futureGet.data().object().toString());
    }
}
