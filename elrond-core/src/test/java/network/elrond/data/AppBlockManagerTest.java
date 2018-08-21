package network.elrond.data;

import javafx.util.Pair;
import junit.framework.TestCase;
import network.elrond.UtilTest;
import network.elrond.account.*;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.NTPClient;
import network.elrond.chronology.Round;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.MultiSignatureService;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import network.elrond.sharding.ShardingServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppBlockManagerTest {
    static AppBlockManager appBlockManager;
    static AppState state;
    static AppContext context;
    static Accounts accounts;
    static AccountsContext accountsContext;
    static PublicKey publicKey;
    static PrivateKey privateKey;
    static Blockchain blockchain;
    static P2PConnection connection = null;
    static PublicKey publicKeyMinting;
    static PrivateKey privateKeyMinting;

    static boolean initialized = false;

    private static final Logger logger = LogManager.getLogger(AppBlockManagerTest.class);

    @Before
    //do one time per class
    public void setupTest() throws Exception {
        if (initialized) {
            return;
        }

        initialized = true;

        AppServiceProvider.getShardingService().setNumberOfShards(1);


        context = new AppContext();
        //context.setMasterPeerIpAddress("");
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setNodeName("Producer and main node");
        context.setStorageBasePath("producer");
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);
        context.setPrivateKey(new PrivateKey("PRODUCER"));

        //Block blk0 = new Block();
        state = new AppState();

        if (connection == null) {
            connection = AppServiceProvider.getP2PConnectionService().createConnection(context);
        }
        state.setConnection(connection);
        connection.setShard(new Shard(0));
        state.setShard(new Shard(0));

        BlockchainContext blockchainContext = new BlockchainContext();
        blockchainContext.setConnection(state.getConnection());
        blockchainContext.setShard(new Shard(0));
        blockchain = new Blockchain(blockchainContext);

        state.setBlockchain(blockchain);
        state.setStillRunning(false);
        state.setNtpClient(new NTPClient(context.getListNTPServers(), 100));
        Thread.sleep(300);

        UtilTest.createDummyGenesisBlock(state.getBlockchain());
        blockchain.setCurrentBlock(blockchain.getGenesisBlock());

        //memory-only accounts
        accountsContext = new AccountsContext();
        accountsContext.setShard(new Shard(0));
        state.setAccounts(new Accounts(accountsContext, new AccountsPersistenceUnit<>(accountsContext.getDatabasePath(), 100)));

        privateKey = new PrivateKey("Receiver");
        publicKey = new PublicKey(privateKey);

        publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        privateKeyMinting = AppServiceProvider.getShardingService().getPrivateKeyForMinting(new Shard(0));

        String hashString = AppServiceProvider.getSerializationService().getHashString(blockchain.getGenesisBlock());
        AppServiceProvider.getBootstrapService().commitBlock(blockchain.getGenesisBlock(), hashString, blockchain);

        accounts = new Accounts(accountsContext, new AccountsPersistenceUnit<>(accountsContext.getDatabasePath(), 100));

        appBlockManager = new AppBlockManager();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testInitialAccounts() throws Exception {
        AccountsContext accountsContextLocal = new AccountsContext();
        accountsContextLocal.setShard(new Shard(0));
        state.setAccounts(new Accounts(accountsContextLocal, new AccountsPersistenceUnit<>(accountsContextLocal.getDatabasePath(), 100)));
        state.setShard(AppServiceProvider.getShardingService().getShard(publicKeyMinting.getValue()));
        //size should be 1
        TestCase.assertEquals("Accounts size should have been 1: ", 1, state.getAccounts().getAddresses().size());

        //the only account should be the mint address and should have the proper value
        List<AccountAddress> listAccountsAddress = new ArrayList<>(state.getAccounts().getAddresses());
        AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(listAccountsAddress.get(0), state.getAccounts());
        TestCase.assertTrue(Arrays.equals(AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0)).getValue(), listAccountsAddress.get(0).getBytes()));

        TestCase.assertEquals(Util.VALUE_MINTING, accountState.getBalance());

        UtilTest.printAccountsWithBalance(state.getAccounts());
    }

    @Test
    public void testProposeAndExecuteBlock() throws Exception {
        //state.setAccounts(accounts);

        AccountsContext accountsContextLocal = new AccountsContext();
        accountsContextLocal.setShard(new Shard(0));
        state.setAccounts(new Accounts(accountsContextLocal, new AccountsPersistenceUnit<>(accountsContextLocal.getDatabasePath(), 100)));

        PrivateKey pvkeyRecv = new PrivateKey("RECV");
        PublicKey pbkeyRecv = new PublicKey(pvkeyRecv);

        Transaction tx1 = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv, BigInteger.TEN, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx1, privateKeyMinting.getValue(), publicKeyMinting.getValue());

        List<Pair<String, Transaction>> transactionHashPairs = new ArrayList<>();
        String txHash = AppServiceProvider.getSerializationService().getHashString(tx1);

        transactionHashPairs.add(new Pair<>(txHash, tx1));
        AppServiceProvider.getBootstrapService().commitTransaction(tx1, txHash, state.getBlockchain());

        waitToStartInAnewRound();
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(transactionHashPairs, state);
        Block blk = blockReceiptsPair.getKey();
        appBlockManager.signBlock(blk, pvkeyRecv);

        UtilTest.printAccountsWithBalance(state.getAccounts());

        TestCase.assertNotNull("Should have returned a block: ", blk);
        TestCase.assertEquals("Should have been 1 account", 1, getValidAccounts(state.getAccounts()));
        TestCase.assertEquals(Util.VALUE_MINTING, getBalance(publicKeyMinting));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(publicKeyMinting));

        ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain(), state.getStatisticsManager());

        TestCase.assertTrue("Should have executed!", executionReport.isOk());
        TestCase.assertEquals(Util.VALUE_MINTING.subtract(BigInteger.TEN), getBalance(publicKeyMinting));
        TestCase.assertEquals(BigInteger.ONE, getNonce(publicKeyMinting));

        TestCase.assertEquals(BigInteger.TEN, getBalance(pbkeyRecv));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(pbkeyRecv));

        UtilTest.printAccountsWithBalance(state.getAccounts());

    }

    @Test
    public void testProposeAndExecuteBlock1BadTx() throws Exception {
        //state.setAccounts(accounts);

        AccountsContext accountsContextLocal = new AccountsContext();
        accountsContextLocal.setShard(new Shard(0));
        state.setAccounts(new Accounts(accountsContextLocal, new AccountsPersistenceUnit<>(accountsContextLocal.getDatabasePath(), 100)));

        PrivateKey pvkeyRecv = new PrivateKey("RECV");
        PublicKey pbkeyRecv = new PublicKey(pvkeyRecv);

        Transaction tx1 = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv, BigInteger.TEN, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx1, privateKeyMinting.getValue(), publicKeyMinting.getValue());

        Transaction tx2 = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv, BigInteger.TEN, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx1, privateKeyMinting.getValue(), publicKeyMinting.getValue());

        List<Pair<String, Transaction>> transactionHashPairs = new ArrayList<>();
        String tx1Hash = AppServiceProvider.getSerializationService().getHashString(tx1);
        String tx2Hash = AppServiceProvider.getSerializationService().getHashString(tx2);

        transactionHashPairs.add(new Pair<>(tx1Hash, tx1));
        transactionHashPairs.add(new Pair<>(tx2Hash, tx2));
        AppServiceProvider.getBootstrapService().commitTransaction(tx1, tx1Hash, state.getBlockchain());
        AppServiceProvider.getBootstrapService().commitTransaction(tx2, tx2Hash, state.getBlockchain());

        waitToStartInAnewRound();
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(transactionHashPairs, state);
        Block blk = blockReceiptsPair.getKey();
        appBlockManager.signBlock(blk, pvkeyRecv);

        UtilTest.printAccountsWithBalance(state.getAccounts());

        TestCase.assertNotNull("Should have returned a block: ", blk);
        TestCase.assertEquals("Should have been 1 account", 1, getValidAccounts(state.getAccounts()));
        TestCase.assertEquals(Util.VALUE_MINTING, getBalance(publicKeyMinting));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(publicKeyMinting));

        ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain(), state.getStatisticsManager());

        TestCase.assertTrue("Should have executed!", executionReport.isOk());
        TestCase.assertEquals(Util.VALUE_MINTING.subtract(BigInteger.TEN), getBalance(publicKeyMinting));
        TestCase.assertEquals(BigInteger.ONE, getNonce(publicKeyMinting));

        TestCase.assertEquals(BigInteger.TEN, getBalance(pbkeyRecv));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(pbkeyRecv));

        UtilTest.printAccountsWithBalance(state.getAccounts());
    }

    @Test
    public void testProposeAndExecuteBlock2BadTx3OK() throws Exception {
        //state.setAccounts(accounts);

        AccountsContext accountsContextLocal = new AccountsContext();
        accountsContextLocal.setShard(new Shard(0));
        state.setAccounts(new Accounts(accountsContextLocal, new AccountsPersistenceUnit<>(accountsContextLocal.getDatabasePath(), 100)));

        PrivateKey pvkeyRecv1 = new PrivateKey("RECV1");
        PublicKey pbkeyRecv1 = new PublicKey(pvkeyRecv1);

        PrivateKey pvkeyRecv2 = new PrivateKey("RECV2");
        PublicKey pbkeyRecv2 = new PublicKey(pvkeyRecv2);

        List<Transaction> transactions = new ArrayList<>();
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (nonce mismatch)
        //transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN.pow(100), BigInteger.ONE));
        //valid transaction to recv2
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv2, BigInteger.TEN, BigInteger.ONE));
        //not valid transaction to recv2 (nonce mismatch)
        //transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv2, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv2 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv2, BigInteger.TEN.pow(100), BigInteger.ZERO));
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN, BigInteger.valueOf(2)));

        List<Pair<String, Transaction>> transactionHashPair = new ArrayList<>();
        for (Transaction transaction : transactions) {
            String txHash = AppServiceProvider.getSerializationService().getHashString(transaction);
            transactionHashPair.add(new Pair<>(txHash, transaction));
            AppServiceProvider.getTransactionService().signTransaction(transaction, privateKeyMinting.getValue(), publicKeyMinting.getValue());

            AppServiceProvider.getBootstrapService().commitTransaction(transaction, txHash, state.getBlockchain());

        }

        waitToStartInAnewRound();
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(transactionHashPair, state);
        Block blk = blockReceiptsPair.getKey();
        appBlockManager.signBlock(blk, pvkeyRecv1);

        UtilTest.printAccountsWithBalance(state.getAccounts());

        TestCase.assertNotNull("Should have returned a block: ", blk);
        TestCase.assertEquals("Should have been 1 account", 1, getValidAccounts(state.getAccounts()));
        TestCase.assertEquals(Util.VALUE_MINTING, getBalance(publicKeyMinting));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(publicKeyMinting));

        ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain(), state.getStatisticsManager());

        TestCase.assertTrue("Should have executed!", executionReport.isOk());

        TestCase.assertEquals(Util.VALUE_MINTING.subtract(BigInteger.valueOf(30)), getBalance(publicKeyMinting));
        TestCase.assertEquals(BigInteger.valueOf(3), getNonce(publicKeyMinting));

        TestCase.assertEquals(BigInteger.valueOf(20), getBalance(pbkeyRecv1));
        TestCase.assertEquals(BigInteger.valueOf(0), getNonce(pbkeyRecv1));

        TestCase.assertEquals(BigInteger.valueOf(10), getBalance(pbkeyRecv2));
        TestCase.assertEquals(BigInteger.valueOf(0), getNonce(pbkeyRecv2));


        UtilTest.printAccountsWithBalance(state.getAccounts());
    }

    @Test
    public void validateBlock() throws Exception {
        //state.setAccounts(accounts);

        AccountsContext accountsContextLocal = new AccountsContext();
        accountsContextLocal.setShard(new Shard(0));
        state.setAccounts(new Accounts(accountsContextLocal, new AccountsPersistenceUnit<>(accountsContextLocal.getDatabasePath(), 100)));

        PrivateKey pvkeyRecv1 = new PrivateKey("RECV1");
        PublicKey pbkeyRecv1 = new PublicKey(pvkeyRecv1);

        PrivateKey pvkeyRecv2 = new PrivateKey("RECV2");
        PublicKey pbkeyRecv2 = new PublicKey(pvkeyRecv2);

        List<Transaction> transactions = new ArrayList<>();
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (nonce mismatch)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN.pow(100), BigInteger.ONE));
        //valid transaction to recv2
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv2, BigInteger.TEN, BigInteger.ONE));
        //not valid transaction to recv2 (nonce mismatch)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv2, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv2 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv2, BigInteger.TEN.pow(100), BigInteger.ZERO));
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, pbkeyRecv1, BigInteger.TEN, BigInteger.valueOf(2)));

        List<Pair<String, Transaction>> transactionHashPair = new ArrayList<>();

        for (Transaction transaction : transactions) {
            String txHash = AppServiceProvider.getSerializationService().getHashString(transaction);
            transactionHashPair.add(new Pair<>(txHash, transaction));
            AppServiceProvider.getTransactionService().signTransaction(transaction, privateKeyMinting.getValue(), publicKeyMinting.getValue());

            AppServiceProvider.getBootstrapService().commitTransaction(transaction, txHash, state.getBlockchain());
        }

        waitToStartInAnewRound();
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(transactionHashPair, state);
        Block block = blockReceiptsPair.getKey();
        appBlockManager.signBlock(block, pvkeyRecv1);

        List<String> signersStringList = block.getListPublicKeys();

        byte[] signature = block.getSignature();
        byte[] commitment = block.getCommitment();
        block.setSignature(null);
        block.setCommitment(null);
        byte[] message = AppServiceProvider.getSerializationService().getHash(block);
        long bitmap = (1 << signersStringList.size()) - 1;

        block.setSignature(signature);
        block.setCommitment(commitment);

        ArrayList<byte[]> signers = new ArrayList<>();
        for (int i = 0; i < signersStringList.size(); i++) {
            signers.add(Util.hexStringToByteArray(signersStringList.get(i)));
        }

        TestCase.assertTrue(AppServiceProvider.getMultiSignatureService().verifyAggregatedSignature(signers, signature, commitment, message, bitmap));

        //test tamper block
        byte[] buff = block.getPrevBlockHash();
        if (buff.length > 0) {
            buff[0] = 'J';
        }
        block.setPrevBlockHash(buff);
        message = AppServiceProvider.getSerializationService().getHash(block);

        TestCase.assertFalse(AppServiceProvider.getMultiSignatureService().verifyAggregatedSignature(signers, signature, commitment, message, bitmap));
    }

    private void waitToStartInAnewRound() {
        long currentTimeStamp = state.getNtpClient().currentTimeMillis();
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();
        Round round = chronologyService.getRoundFromDateTime(blockchain.getGenesisBlock().getTimestamp(), currentTimeStamp);

        int milliSecondsToWait = (int) (chronologyService.getRoundTimeDuration() - (currentTimeStamp - round.getStartTimeStamp()));
        logger.info("Waiting {} ms...", milliSecondsToWait);
        ThreadUtil.sleep(milliSecondsToWait);
    }

    private int getValidAccounts(Accounts accounts) throws Exception {
        int counter = 0;

        for (AccountAddress accountAddress : accounts.getAddresses()) {
            if (AppServiceProvider.getAccountStateService().getAccountState(accountAddress, accounts) != null) {
                counter++;
            }
        }

        return (counter);
    }

    private BigInteger getBalance(PublicKey pbKey) throws Exception {
        if ((state == null) || (pbKey == null)) {
            return (Util.BIG_INT_MIN_ONE);
        }

        AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromBytes(pbKey.getValue()), state.getAccounts());

        if (accountState == null) {
            return (Util.BIG_INT_MIN_ONE);
        }

        return (accountState.getBalance());
    }

    private BigInteger getNonce(PublicKey pbKey) throws Exception {
        if ((state == null) || (pbKey == null)) {
            return (Util.BIG_INT_MIN_ONE);
        }

        AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromBytes(pbKey.getValue()), state.getAccounts());

        if (accountState == null) {
            return (Util.BIG_INT_MIN_ONE);
        }

        return (accountState.getNonce());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComposeBlockWithNullTransactionListShouldThrowException() throws IOException {
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(null, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComposeBlockWithNullApplicationShouldThrowException() throws IOException {
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.emptyList(), null);
    }

    @Test
    public void testComposeBlockWithZeroTransaction() throws IOException {
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.emptyList(), state);
        Block block = blockReceiptsPair.getKey();
        Assert.assertNotNull("Block cannot be null", block);
        Assert.assertNotNull("PrevBlockHash cannot be null", block.prevBlockHash);
        Assert.assertTrue("ListOfTxHashes does not have exactly 0 hash", BlockUtil.isEmptyBlock(block));
    }

    @Test
    public void testComposeBlockWithOneValidTransaction() throws IOException {
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        PrivateKey privateKeyMinting = AppServiceProvider.getShardingService().getPrivateKeyForMinting(new Shard(0));

        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN, BigInteger.ZERO);
        String txHash = AppServiceProvider.getSerializationService().getHashString(tx);
        AppServiceProvider.getTransactionService().signTransaction(tx, privateKeyMinting.getValue(), publicKeyMinting.getValue());
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.singletonList(new Pair<>(txHash, tx)), state);
        Block block = blockReceiptsPair.getKey();
        Assert.assertEquals("ListOfTxHashes does not have exactly 1 hash", 1, BlockUtil.getTransactionsCount(block));
    }

    @Test
    public void testComposeBlockWithOneNotSignedTransaction() throws IOException {
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN.pow(100), BigInteger.ZERO);
        //AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue());
        String txHash = AppServiceProvider.getSerializationService().getHashString(tx);

        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.singletonList(new Pair<>(txHash, tx)), state);
        Block block = blockReceiptsPair.getKey();
        Assert.assertEquals("ListOfTxHashes does not have exactly 0 hash", 0, BlockUtil.getTransactionsCount(block));
    }

    @Test
    public void testComposeBlockWithOneNotEnoughFundsTransaction() throws IOException {
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN.pow(100), BigInteger.ZERO);
        String txHash = AppServiceProvider.getSerializationService().getHashString(tx);
        AppServiceProvider.getTransactionService().signTransaction(tx, privateKeyMinting.getValue(), publicKeyMinting.getValue());
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.singletonList(new Pair<>(txHash, tx)), state);
        Block block = blockReceiptsPair.getKey();
        Assert.assertEquals("ListOfTxHashes does not have exactly 0 hash", 0, BlockUtil.getTransactionsCount(block));
    }

    //TODO: Readd when NonceIsVerified
    //@Test
    public void testComposeBlockWithOneNonceMismatchTransaction() throws IOException {
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN, BigInteger.TEN);
        String txHash = AppServiceProvider.getSerializationService().getHashString(tx);
        AppServiceProvider.getTransactionService().signTransaction(tx, privateKeyMinting.getValue(), publicKeyMinting.getValue());
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.singletonList(new Pair<>(txHash, tx)), state);
        Block block = blockReceiptsPair.getKey();
        Assert.assertEquals("ListOfTxHashes does not have exactly 0 hash", 0, BlockUtil.getTransactionsCount(block));
    }

    @Test
    public void testComposeBlockWithOneValidAndOneInValidTransaction() throws IOException {
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        PrivateKey privateKeyMinting = AppServiceProvider.getShardingService().getPrivateKeyForMinting(new Shard(0));

        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN.pow(100), BigInteger.ZERO);
        String txHash = AppServiceProvider.getSerializationService().getHashString(tx);
        Transaction tx2 = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN.pow(1), BigInteger.ZERO);
        String tx2Hash = AppServiceProvider.getSerializationService().getHashString(tx2);

        AppServiceProvider.getTransactionService().signTransaction(tx, privateKeyMinting.getValue(), publicKeyMinting.getValue());
        AppServiceProvider.getTransactionService().signTransaction(tx2, privateKeyMinting.getValue(), publicKeyMinting.getValue());

        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Arrays.asList(new Pair<>(txHash, tx), new Pair<>(tx2Hash, tx2)), state);
        Block block = blockReceiptsPair.getKey();
        Assert.assertEquals("ListOfTxHashes does not have exactly 1 hash", 1, BlockUtil.getTransactionsCount(block));
    }

    @Test
    public void testComposeBlockWithNoCurrentBlockFoundTransaction() throws IOException {
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.emptyList(), state);
        Block block = blockReceiptsPair.getKey();
        Assert.assertNotNull("Block cannot be null", block);
        Assert.assertNotNull("PrevBlockHash cannot be null", block.prevBlockHash);
        Assert.assertEquals("ListOfTxHashes does not have exactly 0 hash", 0, BlockUtil.getTransactionsCount(block));
    }

    @Test
    public void testSignEmptyBlock() throws IOException {
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.emptyList(), state);
        Block block = blockReceiptsPair.getKey();
        appBlockManager.signBlock(block, privateKey);

        Assert.assertNotNull("Commitment cannot be null", block.getCommitment());
        Assert.assertNotNull("Signature cannot be null", block.getSignature());
        Assert.assertNotNull("Signature cannot be null", block.getListPublicKeys());
    }

    @Test
    public void testVerifySignEmptyBlock() throws IOException {
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.emptyList(), state);
        Block block = blockReceiptsPair.getKey();
        appBlockManager.signBlock(block, privateKey);
        MultiSignatureService multiSignatureService = AppServiceProvider.getMultiSignatureService();

        Assert.assertNotNull("Commitment cannot be null", block.getCommitment());
        Assert.assertNotNull("Signature cannot be null", block.getSignature());
        Assert.assertNotNull("Signature cannot be null", block.getListPublicKeys());
    }

    @Test
    public void testVerifySignatureEmptyBlock() throws IOException {
        accounts = new Accounts(accountsContext, new AccountsPersistenceUnit<>(accountsContext.getDatabasePath(), 100));
        state.setAccounts(accounts);
        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Collections.emptyList(), state);
        Block block = blockReceiptsPair.getKey();

        appBlockManager.signBlock(block, privateKey);

        Assert.assertTrue("Signature is not ok!", VerifySignature(block));
    }

    @Test
    public void testVerifySignatureBlock() throws IOException {
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN.pow(1), BigInteger.ONE);
        String txHash = AppServiceProvider.getSerializationService().getHashString(tx);
        Transaction tx2 = AppServiceProvider.getTransactionService().generateTransaction(publicKeyMinting, publicKey, BigInteger.TEN.pow(1), BigInteger.ONE);
        String tx2Hash = AppServiceProvider.getSerializationService().getHashString(tx);

        AppServiceProvider.getTransactionService().signTransaction(tx, privateKeyMinting.getValue(), publicKeyMinting.getValue());
        AppServiceProvider.getTransactionService().signTransaction(tx2, privateKeyMinting.getValue(), publicKeyMinting.getValue());

        accounts = new Accounts(accountsContext, new AccountsPersistenceUnit<>(accountsContext.getDatabasePath(), 100));

        Pair<Block, List<Receipt>> blockReceiptsPair = appBlockManager.composeBlock(Arrays.asList(new Pair<>(txHash, tx), new Pair<>(tx2Hash, tx2)), state);
        Block block = blockReceiptsPair.getKey();
        appBlockManager.signBlock(block, privateKey);

        Assert.assertTrue("Signature is not ok!", VerifySignature(block));
    }

    public boolean VerifySignature(Block block) {
        ArrayList<byte[]> signersPublicKeys = new ArrayList<>();
        ArrayList<byte[]> commitmentSecrets = new ArrayList<>();
        ArrayList<byte[]> commitments = new ArrayList<>();
        ArrayList<byte[]> challenges = new ArrayList<>();
        ArrayList<byte[]> signatureShares = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] aggregatedSignature = new byte[0];
        int sizeConsensusGroup = 1;
        MultiSignatureService multiSignatureService = AppServiceProvider.getMultiSignatureService();

        byte[][] result = new byte[2][];

        for (int i = 0; i < sizeConsensusGroup; i++) {
            signersPublicKeys.add(new PublicKey(privateKey).getValue());
            commitmentSecrets.add(multiSignatureService.computeCommitmentSecret());
            commitments.add(multiSignatureService.computeCommitment(commitmentSecrets.get(i)));
        }

        byte[] blockHashNoSig = AppServiceProvider.getSerializationService().getHash(block);

        // aggregate the commitments
        aggregatedCommitment = multiSignatureService.aggregateCommitments(commitments, 1);

        // compute challenges and signatures for each signer
        for (int i = 0; i < sizeConsensusGroup; i++) {
            if (0 != ((1 << i) & 1)) {
                challenges.add(
                        multiSignatureService.computeChallenge(
                                signersPublicKeys,
                                signersPublicKeys.get(i),
                                aggregatedCommitment,
                                blockHashNoSig,
                                1
                        )
                );

                // compute signature shares
                signatureShares.add(
                        multiSignatureService.computeSignatureShare(
                                challenges.get(i),
                                privateKey.getValue(),
                                commitmentSecrets.get(i)
                        )
                );
            } else {
                challenges.add(new byte[0]);
                signatureShares.add(new byte[0]);
            }

            aggregatedSignature = multiSignatureService.aggregateSignatures(signatureShares, 1);
        }

        return multiSignatureService.verifyAggregatedSignature(signersPublicKeys, aggregatedSignature, aggregatedCommitment, blockHashNoSig, 1);
    }
}
