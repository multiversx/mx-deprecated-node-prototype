package network.elrond.data;

import network.elrond.SlowTests;
import network.elrond.account.Accounts;
import network.elrond.account.AccountsContext;
import network.elrond.account.AccountsPersistenceUnit;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Category(SlowTests.class)
public class AppBlockManagerIT {

    double N = 100;
    AppBlockManager appBlockManager;
    AppState state;
    AppContext context;
    Accounts accounts;
    AccountsContext accountsContext;
    PublicKey publicKey;
    PrivateKey privateKey;
    Blockchain blockchain;

    @Before
    public void setupTest() throws Exception {
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress(null);
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setNodeName("Producer and main node");
        context.setStorageBasePath("producer");
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);
        context.setPrivateKey(new PrivateKey("PRODUCER"));

        Block blk0 = new Block();
        state = new AppState();

        P2PConnection connection = AppServiceProvider.getP2PBroadcastService().createConnection(context);
        state.setConnection(connection);

        BlockchainContext blockchainContext = new BlockchainContext();
        blockchainContext.setConnection(state.getConnection());
        blockchain = new Blockchain(blockchainContext);
        blockchain.setCurrentBlock(blk0);
        state.setBlockchain(blockchain);
        state.setStillRunning(false);

        //memory-only accounts
        accountsContext = new AccountsContext();
        state.setAccounts(new Accounts(accountsContext, new AccountsPersistenceUnit<>(accountsContext.getDatabasePath())));

        privateKey = new PrivateKey("Receiver");
        publicKey = new PublicKey(privateKey);

        String hashString = AppServiceProvider.getSerializationService().getHashString(blk0);
        AppServiceProvider.getBootstrapService().commitBlock(blk0, hashString, blockchain);

        appBlockManager = new AppBlockManager();
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testGenerateNTransactions() throws IOException {
        long start = System.currentTimeMillis();

        for(int i = 0;i<N;i++){
            Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN, BigInteger.ZERO);
        }
        long end = System.currentTimeMillis();

        WriteLine("Generated: ", end - start);
    }

    @Test
    public void testGenerateAndSignNTransactions() throws IOException {
        long start = System.currentTimeMillis();

        for(int i = 0;i<N;i++){
            Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN, BigInteger.ZERO);
            AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue(), Util.PUBLIC_KEY_MINTING.getValue());
           // AppServiceProvider.getTransactionService().verifyTransaction(tx);
            //AppServiceProvider.getBootstrapService().commitTransaction(tx, AppServiceProvider.getSerializationService().getHashString(tx), blockchain);
        }
        long end = System.currentTimeMillis();

        WriteLine("Generated and signed: ", end - start);
      //  AppBlockManager.instance().generateAndBroadcastBlock(hashes, accounts, blockchain, privateKey);
    }


    @Test
    public void testGenerateSignAndVerifyNTransactions() throws IOException {
        long start = System.currentTimeMillis();

        for(int i = 0;i<N;i++){
            Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN, BigInteger.ZERO);
            AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue(), Util.PUBLIC_KEY_MINTING.getValue());
            AppServiceProvider.getTransactionService().verifyTransaction(tx);
            //AppServiceProvider.getBootstrapService().commitTransaction(tx, AppServiceProvider.getSerializationService().getHashString(tx), blockchain);
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start));
        WriteLine("Generated, signed and verified: ", end - start);
        //  AppBlockManager.instance().generateAndBroadcastBlock(hashes, accounts, blockchain, privateKey);
    }

    @Test
    public void testGenerateSignAndPutInBlockchainNTransactions() throws IOException {
        long start = System.currentTimeMillis();

        List<String> hashes = GenerateTransactionHashes(N);

        long end = System.currentTimeMillis();
        System.out.println((end - start));
        WriteLine("Generated, signed and put in blockchain: ", end - start);
        //  AppBlockManager.instance().generateAndBroadcastBlock(hashes, accounts, blockchain, privateKey);
    }



    @Test
    public void testComposeAndSignNTransactionsInBlock() throws IOException, ClassNotFoundException {

        List<String> hashes = GenerateTransactionHashes(N);

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        accounts = new Accounts(new AccountsContext(), new AccountsPersistenceUnit<>(""));

        long start = System.currentTimeMillis();
        List<Transaction> transactions = blockchainService.getAll(hashes, blockchain, BlockchainUnitType.TRANSACTION);
        Block block = AppBlockManager.instance().composeBlock(transactions, blockchain, accounts, null);
        AppBlockManager.instance().signBlock(block, privateKey);
        long end = System.currentTimeMillis();
        System.out.println((end - start));
        WriteLine("Composed and signed block of  ", end - start);

    }

    @Test
    public void testGenerateAndBroadCastBlock() throws IOException, ClassNotFoundException {

        List<String> hashes = GenerateTransactionHashes(N);

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        accounts = new Accounts(new AccountsContext(), new AccountsPersistenceUnit<>(""));

        long start = System.currentTimeMillis();

        AppBlockManager.instance().generateAndBroadcastBlock(hashes, accounts, blockchain, privateKey, null);

        long end = System.currentTimeMillis();
        System.out.println((end - start));
        WriteLine("Generated and broadcasted block of ", end - start);

    }

    private List<String> GenerateTransactionHashes(double nrTransactions) {
        List<String> hashes = new ArrayList<String>();
        for(int i = 0;i<nrTransactions;i++){
            Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN, BigInteger.ZERO);
            AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue(), Util.PUBLIC_KEY_MINTING.getValue());
            String hash = AppServiceProvider.getSerializationService().getHashString(tx);
            hashes.add(hash);
            //AppServiceProvider.getTransactionService().verifyTransaction(tx);
            AppServiceProvider.getBootstrapService().commitTransaction(tx, hash, blockchain);
        }
        return hashes;
    }

    private void WriteLine(String message, long time){
        System.out.println(String.format("%s %.2f transactions in %d miliseconds at %.2f TPS",message, N, time,  N*1000/time));
    }
}
