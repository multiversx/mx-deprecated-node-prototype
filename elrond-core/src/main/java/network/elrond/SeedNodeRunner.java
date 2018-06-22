package network.elrond;

import net.tomp2p.peers.PeerAddress;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.BootstrapType;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.List;

public class SeedNodeRunner {
    private static final Logger logger = LogManager.getLogger(SeedNodeRunner.class);

    public static void main(String[] args) throws Exception {

        String nodeName = "elrond-node-1";
        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String seedNodeRunnerPrivateKey = "1111111111111111fa612ecafcfd145cc06c1fb64d7499ef34696ff16b82cbc2";

        PublicKey pbKey = new PublicKey(new PrivateKey(seedNodeRunnerPrivateKey));
        //Reuploaded
        AppContext context = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);


        Thread thread = new Thread(() -> {

            do {
                AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
                Transaction transaction = facade.send(address, BigInteger.TEN, application);
                logger.info("Balance: {}", facade.getBalance(address, application));

                if (transaction != null) {
                    String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
                    Receipt receipt = facade.getReceipt(hash, application);
                    logger.info(receipt);
                }

                printPeers(application);

                ThreadUtil.sleep(1000);
            } while (true);

        });
        thread.start();

    }

    public static void printPeers(Application application){
        AppState appState = application.getState();

        if (appState.getConnection() == null){
            System.out.println("connection null!");
            return;
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("Network size: %d", appState.getConnection().getPeer().peerBean().peerMap().all().size() + 1));
        System.out.println(String.format("Current peer ID: %s", appState.getConnection().getPeer().peerID().toString()));

        List<PeerAddress> listPeers = appState.getConnection().getPeer().peerBean().peerMap().all();

        for (int i = 0; i < listPeers.size(); i++){
            System.out.println(String.format("Peer ID: %s", listPeers.get(i).peerId().toString()));
        }

        System.out.println("----------------------------------------------------------------------");
    }

}
