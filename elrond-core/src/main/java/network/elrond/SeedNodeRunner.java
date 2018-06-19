package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.BootstrapType;

import java.math.BigInteger;

public class SeedNodeRunner {

    public static void main(String[] args) throws Exception {

        String nodeName = "elrond-node-1";
        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String seedNodeRunnerPrivateKey = "1111111111111111fa612ecafcfd145cc06c1fb64d7499ef34696ff16b82cbc2";

        PublicKey pbKey = new PublicKey(new PrivateKey(seedNodeRunnerPrivateKey));

        AppContext context = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);


        Thread thread = new Thread(() -> {

            do {

                AccountAddress address = AccountAddress.fromBytes(pbKey.getValue());
                facade.send(address, BigInteger.TEN, application);
                System.out.println(facade.getBalance(address, application));

                ThreadUtil.sleep(1000);
            } while (true);

        });
        thread.start();

    }
}
