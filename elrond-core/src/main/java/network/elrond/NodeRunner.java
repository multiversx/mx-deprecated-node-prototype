package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.BootstrapType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class NodeRunner {

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdfSource = new SimpleDateFormat(
                "yyyy-MM-dd HH.mm.ss");
        Util.changeLogsPath("logs/" + Util.getHostName() + " - " + sdfSource.format(new Date()));

        Random rand = new Random();
        int nr = rand.nextInt(1000);

        String nodeName = "elrond-node-2.1" + nr;
        Integer port = 4001 + nr;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String nodeRunnerPrivateKey = "151b9ec1ad1f44c9c835c3dae8826e645ebcc00195396b35fcb4241e9a40533c";
        if (nodeRunnerPrivateKey.length() % 2 == 1) {
            nodeRunnerPrivateKey += "1";
        }
        //Reuploaded
        AppContext context = ContextCreator.createAppContext(nodeName, nodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);

        Thread thread = new Thread(() -> {

            do {

                AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
                //facade.send(address, BigInteger.TEN, application);
                ThreadUtil.sleep(10000);
                System.out.println(facade.getBalance(address, application));
            } while (true);

        });
        thread.start();
    }
}
