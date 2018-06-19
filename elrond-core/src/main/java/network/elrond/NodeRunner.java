package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.BootstrapType;

public class NodeRunner {

    public static void main(String[] args) throws Exception {

        String nodeName = "elrond-node-2";
        Integer port = 4001;
        Integer masterPeerPort = 31201;
        String masterPeerIpAddress = "127.0.0.1";
        String nodeRunnerPrivateKey = "1df1e9456051e43cfa612ecafcfd145cc06c1fb64d7499ef34696ff16b82cbc2";

        AppContext context = ContextCreator.createAppContext(nodeName, nodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName );

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);

        Thread thread = new Thread(() -> {

            do {

                AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
                //facade.send(address, BigInteger.TEN, application);
                System.out.println(facade.getBalance(address, application));
                ThreadUtil.sleep(2000);


            } while (true);

        });
        thread.start();
    }
}
