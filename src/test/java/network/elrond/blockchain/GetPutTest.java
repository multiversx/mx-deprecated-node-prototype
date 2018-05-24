package network.elrond.blockchain;

import net.tomp2p.dht.FuturePut;
import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.SerializationService;
import network.elrond.data.Transaction;
import network.elrond.data.TransactionService;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;

public class GetPutTest {
    private TransactionService transactionService = AppServiceProvider.getTransactionService();
    private SerializationService serializationService = AppServiceProvider.getSerializationService();

    private PrivateKey pvKeySender = new PrivateKey("a");
    private PublicKey pbKeySender = new PublicKey(pvKeySender);

    private PrivateKey pvKeyRecv = new PrivateKey("b");
    private PublicKey pbKeyRecv = new PublicKey(pvKeyRecv);

    //@Test
    public void mainProducer() throws Exception {

        AppContext context = new AppContext();
        context.setStorageBasePath("producer");
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setPeerId(1);


        Application app = new Application(context);
        app.start();

        Thread thread = new Thread(() -> {

            AppState state = app.getState();
            P2PBroadcastChanel channel = state.getChanel("TRANSACTIONS");

            int value = 0;

            SerializationService serServ = AppServiceProvider.getSerializationService();

            do {
                if (value < 1000) {
                    //inject transaction based on value so I will know the hash on the other node
                    Transaction tx = generateTransaction(value);

                    try {
                        FuturePut fp = AppServiceProvider.getP2PObjectService().put(channel.getConnection(),
                                getTxHash(tx), serServ.encodeJSON(tx));
                        System.out.println("Pet tx hash: " + getTxHash(tx) + " on wire...");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    value++;
                }

                //InjectBlk(state, rdm);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } while (state.isStillRunning());

        });
        thread.start();


        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            if (input.nextLine().equals("exit")) {
                app.stop();
            }
        }
    }

    //@Test
    public void mainConsumer() throws Exception {

        AppContext context = new AppContext();
        context.setStorageBasePath("consumer");
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001);
        context.setPeerId(0);


        Application app = new Application(context);
        app.start();

        PrivateKey pvKey = new PrivateKey();
        PublicKey pbKey = new PublicKey(pvKey);

        Random rdm = new Random(10);

        Thread thread = new Thread(() -> {

            AppState state = app.getState();

            BlockchainService blockchainService = AppServiceProvider.getBlockchainService();


            BlockchainContext blockchainContext = new BlockchainContext();
            blockchainContext.setConnection(state.getConnection());
            blockchainContext.setDatabasePath(BlockchainUnitType.BLOCK, "blockchain.block.data-test");
            blockchainContext.setDatabasePath(BlockchainUnitType.TRANSACTION, "blockchain.transaction.data-test");
            blockchainContext.setDatabasePath(BlockchainUnitType.SETTINGS, "blockchain.settings.data-test");

            Blockchain blockchain = null;

            try {
                blockchain = new Blockchain(blockchainContext);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int value = 0;

            do {
                if (value < 1000) {
                    Transaction txExpected = generateTransaction(value);
                    String hash = getTxHash(txExpected);

                    System.out.println("Trying to getAccountState tx hash: " + hash + "...");

                    Transaction tx = null;

                    try {
                        tx = blockchainService.get(hash, blockchain, BlockchainUnitType.TRANSACTION);


                        if (tx != null) {
                            System.out.println("Hash: " + hash + " got it!");
                            value++;
                        } else {
                            System.out.println("Hash: " + hash + ", value: " + value + ", NULL");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("Hash: " + hash + ", value: " + value + ", ex: " + ex.getMessage());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } while (state.isStillRunning());

        });
        thread.start();


        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            if (input.nextLine().equals("exit")) {
                app.stop();
            }
        }

    }

    private Transaction generateTransaction(int value) {
        Transaction transaction = new Transaction();
        transaction.setNonce(BigInteger.ZERO);
        //2 ERDs
        transaction.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(value)));
        transaction.setSendAddress(Util.getAddressFromPublicKey(pbKeySender.getEncoded()));
        transaction.setReceiverAddress(Util.getAddressFromPublicKey(pbKeyRecv.getEncoded()));
        transaction.setPubKey(Util.byteArrayToHexString(pbKeySender.getEncoded()));

        //transactionService.signTransaction(transaction, pvKeySender.getValue());

        return (transaction);
    }

    private String getTxHash(Transaction tx) {
        return (new String(Base64.encode(serializationService.getHash(tx, true))));
    }
}
