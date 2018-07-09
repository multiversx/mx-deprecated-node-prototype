package network.elrond.p2p;

import junit.framework.TestCase;
import net.tomp2p.dht.*;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import network.elrond.ContextCreator;
import network.elrond.application.AppContext;
import network.elrond.core.ByteUtil;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.BootstrapType;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.ShardingServiceImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class P2PObjectServiceTest {

    public String strKey = "";

    @Test
    public void TestPutIfAbsent () throws Exception {
        StartSeeder();
        StartRunner();
        StartKeyGenerator();
    }

    public void StartKeyGenerator() throws Exception {
        Logger logger = LoggerFactory.getLogger(P2PObjectServiceTest.class);
            int n = 0;

            do {
                ThreadUtil.sleep(1000);
                n++;
//                strKey = String.valueOf(System.currentTimeMillis());
                strKey = String.valueOf(n);
//                System.out.println("0x" + strKey);
                logger.info("New key generated: 0x" + strKey);
            } while (n < 30);

            strKey = "";
    }

    public void StartSeeder() throws Exception {
        Thread thread = new Thread(() -> {
            try {
                while (strKey.isEmpty()) {
                    Thread.sleep(1);
                }

                Logger logger = LoggerFactory.getLogger(P2PObjectServiceTest.class);
                String nodeName = "elrond-seeder";

                Integer port = 4000;
                Integer masterPeerPort = 4000;
                String masterPeerIpAddress = "127.0.0.1";
                String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey("elrond-node-1").getValue());

                AppContext appContext = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                        BootstrapType.START_FROM_SCRATCH, nodeName);

                P2PConnection connection = AppServiceProvider.getP2PConnectionService().createConnection(appContext);

                PeerDHT peerDHT = connection.getDht();

                int n = 0;

                do {
                    Thread.sleep(1);
                    n++;

                    String strLocalKey = "0x" + strKey;
                    String strValue = nodeName + "-" + n;
//                    String strValue = nodeName;

                    logger.info(nodeName + " start runda " + n + ": try to put at key " + strLocalKey + " -> value " + strValue);

                    if (strKey.isEmpty()) break;

                    PutBuilder putBuilder = peerDHT.put(new Number160(strLocalKey));
                    putBuilder.data(new Data(strValue));
                    putBuilder.putIfAbsent();
                    FuturePut futurePut = putBuilder.start().awaitUninterruptibly();

                    GetBuilder getBuilder = peerDHT.get(new Number160(strLocalKey));
                    FutureGet futureGet = getBuilder.start().awaitUninterruptibly();

                    String strCurrentValue = futureGet.data().object().toString();

                    logger.info(nodeName + " stop runda " + n + ": put at key " + strLocalKey + " -> value " + strValue + " and get value " + strCurrentValue + " with success " + futurePut.isSuccess());

                    if (futurePut.isSuccess()) {
//                    System.out.println(strLocalKey + " - " + strValue);
                        logger.info(strLocalKey + " - " + strValue);

                        TestCase.assertEquals(strValue, strCurrentValue);
                    } else {

                        if (strValue.equals(strCurrentValue))
                        {
                            logger.info("#######################################################################################");
                            logger.info(nodeName + " -> Key = " + strLocalKey + ", Value = " + strValue + ", Current value = " + strCurrentValue);
                            logger.info("#######################################################################################");
                        }

                        TestCase.assertFalse("Expect not to be equal for key " + strLocalKey + ": (" + strValue + ", " + strCurrentValue + ")" , strValue.equals(strCurrentValue));
                    }
                } while (true);
            }
            catch (Exception e) {
            }
        });

        thread.start();
    }

    public void StartRunner() throws Exception {
        Thread thread = new Thread(() -> {
            try {
                while (strKey.isEmpty()) {
                    Thread.sleep(1);
                }

                Logger logger = LoggerFactory.getLogger(P2PObjectServiceTest.class);
                String nodeName = "elrond-runner";

                Integer port = 4001;
                Integer masterPeerPort = 4000;
                String masterPeerIpAddress = "127.0.0.1";
                String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey("elrond-node-2.1").getValue());

                AppContext appContext = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                        BootstrapType.START_FROM_SCRATCH, nodeName);

                P2PConnection connection = AppServiceProvider.getP2PConnectionService().createConnection(appContext);

                PeerDHT peerDHT = connection.getDht();

                int n = 0;

                do {
                    Thread.sleep(1);
                    n++;

                    String strLocalKey = "0x" + strKey;
                    String strValue = nodeName + "-" + n;
//                    String strValue = nodeName;

                    logger.info(nodeName + " start runda " + n + ": try to put at key " + strLocalKey + " -> value " + strValue);

                    if (strKey.isEmpty()) break;

                    PutBuilder putBuilder = peerDHT.put(new Number160(strLocalKey));
                    putBuilder.data(new Data(strValue));
                    putBuilder.putIfAbsent();
                    FuturePut futurePut = putBuilder.start().awaitUninterruptibly();

                    GetBuilder getBuilder = peerDHT.get(new Number160(strLocalKey));
                    FutureGet futureGet = getBuilder.start().awaitUninterruptibly();

                    String strCurrentValue = futureGet.data().object().toString();

                    logger.info(nodeName + " stop runda " + n + ": put at key " + strLocalKey + " -> value " + strValue + " and get value " + strCurrentValue + " with success " + futurePut.isSuccess());

                    if (futurePut.isSuccess()) {
//                    System.out.println(strLocalKey + " - " + strValue);
                        logger.info(strLocalKey + " - " + strValue);

                        TestCase.assertEquals(strValue, strCurrentValue);
                    } else {

                        if (strValue.equals(strCurrentValue))
                        {
                            logger.info("#######################################################################################");
                            logger.info(nodeName + " -> Key = " + strLocalKey + ", Value = " + strValue + ", Current value = " + strCurrentValue);
                            logger.info("#######################################################################################");
                        }

                        TestCase.assertFalse("Expect not to be equal for key " + strLocalKey + ": (" + strValue + ", " + strCurrentValue + ")" , strValue.equals(strCurrentValue));
                    }
                } while (true);
            }
            catch (Exception e) {
            }
        });

        thread.start();
    }

    @Test
    public void testPutIfAbsentOnDHT() throws Exception{
        String nodeName = "elrond-seeder";

        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey("elrond-node-1").getValue());

        AppContext appContext =  ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
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
