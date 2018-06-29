package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.BootstrapType;
import network.elrond.data.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;

public class SeedNodeRunner {
    private static final Logger logger = LogManager.getLogger(SeedNodeRunner.class);

    public static void main(String[] args) throws Exception {

        String nodeName = "elrond-node-1";
        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String seedNodeRunnerPrivateKey = "00948c6246ebb299414ccd3cc8b17674d3f6fe0d14b984b6c2c84e0d5866a38da2";

        AppContext context = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);

        Thread.sleep(5000);

        Thread thread = new Thread(() -> {
            int nrTranzactii = 0;

            long crtTimeMillis = System.currentTimeMillis();

            do {
                AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
                if (nrTranzactii < 2000) {
                    Transaction transaction = facade.send(address, BigInteger.TEN, application);
                    nrTranzactii++;
                } else {
                    ThreadUtil.sleep(1);
                }

                if (System.currentTimeMillis() - crtTimeMillis > 1000){
                    crtTimeMillis = System.currentTimeMillis();

                    logger.error("Transactions sent: {}", nrTranzactii);
                }

                //logger.info("Sender Balance: {}", facade.getBalance(AccountAddress.fromBytes(application.getState().getPublicKey().getValue()), application));

                //logger.info("Receiver  Balance: {}", facade.getBalance(address, application));

//                if (transaction != null) {
//                    String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
//                    Receipt receipt = facade.getReceipt(hash, application);
//                    logger.info(receipt);
//                }

                //ThreadUtil.sleep(1);
            } while (true);

        });
        thread.setPriority(2);
        thread.start();

    }
}
