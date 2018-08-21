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
import network.elrond.sharding.ShardingServiceImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class P2PObjectServiceRunnerTest {

    private String strKey = "";
    private final int MAX_GENERATED_KEYS = 60;
    private List<String> strKeys = new ArrayList<>();

    @Test
    public void TestPutIfAbsent () throws Exception {
        AppServiceProvider.getShardingService().setNumberOfShards(1);
        StartRunner();
        StartKeyGenerator();
    }

    public void StartKeyGenerator() throws Exception {
        Logger logger = LoggerFactory.getLogger(P2PObjectServiceTest.class);
        String strOldKey = "";

        do {
            do {
                Thread.sleep(1);
                strKey = String.valueOf(System.currentTimeMillis() / 1000);
                if (!strKey.equals(strOldKey))
                {
                    strOldKey = strKey;
                    break;
                }
            } while(true);
            strKeys.add(strKey);
            logger.info("New key generated: 0x" + strKey);
        } while (strKeys.size() < MAX_GENERATED_KEYS);

        strKey = "";
        Thread.sleep(5000);
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
                String masterPeerIpAddress = "192.168.11.32";
                String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey("elrond-node-2.1").getValue());

                AppContext appContext = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                        BootstrapType.START_FROM_SCRATCH, nodeName, false);

                P2PConnection connection = AppServiceProvider.getP2PConnectionService().createConnection(appContext);

                PeerDHT peerDHT = connection.getDht();

                int n = 0;

                do {
                    Thread.sleep(1);
                    n++;

                    String strLocalKey = "0x" + strKey;
                    String strValue = nodeName + "-" + n;
//                    String strValue = nodeName;

                    logger.info(nodeName + " start round " + n + ": try to put at key: " + strLocalKey + " -> value: " + strValue);

                    if (strKey.isEmpty()) break;

                    PutBuilder putBuilder = peerDHT.put(new Number160(strLocalKey));
                    putBuilder.data(new Data(strValue));
                    putBuilder.putIfAbsent();
                    FuturePut futurePut = putBuilder.start().awaitUninterruptibly();

                    GetBuilder getBuilder = peerDHT.get(new Number160(strLocalKey));
                    FutureGet futureGet = getBuilder.start().awaitUninterruptibly();

                    String strCurrentValue = futureGet.data().object().toString();

                    logger.info(nodeName + " stop round " + n + ": put at key: " + strLocalKey + " -> value: " + strValue + " and got value: " + strCurrentValue + " with success: " + futurePut.isSuccess() + " and partially success: " + futurePut.isSuccessPartially());

                    if (futurePut.isSuccess()) {
//                    if (futurePut.isSuccessPartially()) {
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

//                        TestCase.assertFalse("Expect not to be equal for key " + strLocalKey + ": (" + strValue + ", " + strCurrentValue + ")" , strValue.equals(strCurrentValue));
                    }
                } while (true);

                for (String key : strKeys)
                {
                    String strLocalKey = "0x" + key;

                    GetBuilder getBuilder = peerDHT.get(new Number160(strLocalKey));
                    FutureGet futureGet = getBuilder.start().awaitUninterruptibly();

                    if (futureGet.data() == null) {
                        logger.info(nodeName + " -> Key = " + strLocalKey + ", Value = null");
                        continue;
                    }

                    String strCurrentValue = futureGet.data().object().toString();

                    logger.info(nodeName + " -> Key = " + strLocalKey + ", Value = " + strCurrentValue);
                }
            }
            catch (Exception e) {
            }
        });

        thread.start();
    }
}
