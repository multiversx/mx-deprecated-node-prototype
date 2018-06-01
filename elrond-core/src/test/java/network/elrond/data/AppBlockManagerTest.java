package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.Application;
import network.elrond.UtilTest;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.account.AccountsContext;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.crypto.MultiSignatureService;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.junit.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppBlockManagerTest {
    static AppBlockManager appBlockManager;
    static Application application;
    static AppState state;
    static AccountsContext accountsContext;
    static PublicKey publicKey;
    static PrivateKey privateKey;

    @BeforeClass
    public static void setupTest() throws Exception{
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress(null);
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setNodeName("Producer and main node");
        context.setStorageBasePath("producer");
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);
        context.setPrivateKey(new PrivateKey("PRODUCER"));

        application = new Application(context);
        state = application.getState();
        state.setStillRunning(false);
        Block blk0 = new Block();
        state.setCurrentBlock(blk0);
        application.start();

        //memory-only accounts
        accountsContext = new AccountsContext();
        state.setAccounts(new Accounts(accountsContext));

        privateKey = new PrivateKey("Receiver");
        publicKey = new PublicKey(privateKey);

        AppServiceProvider.getBootstrapService().putBlockInBlockchain(blk0,
                AppServiceProvider.getSerializationService().getHashString(blk0), state);

        appBlockManager = new AppBlockManager();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        application.stop();
    }

    @Test
    public void testInitialAccounts() throws Exception{
        state.setAccounts(new Accounts(accountsContext));

        //size should be 1
        TestCase.assertEquals("Accounts size should have been 1: ", 1, state.getAccounts().getAddresses().size());

        //the only account should be the mint address and should have the proper value
        List<AccountAddress> listAccountsAddress = new ArrayList<>(state.getAccounts().getAddresses());
        AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(listAccountsAddress.get(0), state.getAccounts());
        TestCase.assertTrue(Arrays.equals(Util.PUBLIC_KEY_MINTING.getValue(), listAccountsAddress.get(0).getBytes()));

        TestCase.assertEquals(Util.VALUE_MINTING, accountState.getBalance());

        UtilTest.printAccountsWithBalance(state.getAccounts());
    }

    @Test
    public void testProposeAndExecuteBlock() throws Exception{
        state.setAccounts(new Accounts(accountsContext));

        PrivateKey pvkeyRecv = new PrivateKey("RECV");
        PublicKey pbkeyRecv = new PublicKey(pvkeyRecv);

        Transaction tx1 = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv, BigInteger.TEN, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx1, Util.PRIVATE_KEY_MINTING.getValue());

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(tx1);
        AppServiceProvider.getBootstrapService().putTransactionInBlockchain(tx1, AppServiceProvider.getSerializationService().getHashString(tx1), state);

        Block blk = appBlockManager.composeBlock(transactions, application);
        appBlockManager.signBlock(blk, pvkeyRecv);

        UtilTest.printAccountsWithBalance(state.getAccounts());

        TestCase.assertNotNull("Should have returned a block: ", blk);
        TestCase.assertEquals("Should have been 1 account", 1, getValidAccounts(state.getAccounts()));
        TestCase.assertEquals(Util.VALUE_MINTING, getBalance(Util.PUBLIC_KEY_MINTING));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(Util.PUBLIC_KEY_MINTING));

        ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

        TestCase.assertTrue("Should have executed!", executionReport.isOk());
        TestCase.assertEquals(Util.VALUE_MINTING.subtract(BigInteger.TEN), getBalance(Util.PUBLIC_KEY_MINTING));
        TestCase.assertEquals(BigInteger.ONE, getNonce(Util.PUBLIC_KEY_MINTING));

        TestCase.assertEquals(BigInteger.TEN, getBalance(pbkeyRecv));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(pbkeyRecv));

        UtilTest.printAccountsWithBalance(state.getAccounts());


    }

    @Test
    public void testProposeAndExecuteBlock1BadTx() throws Exception{
        state.setAccounts(new Accounts(accountsContext));

        PrivateKey pvkeyRecv = new PrivateKey("RECV");
        PublicKey pbkeyRecv = new PublicKey(pvkeyRecv);

        Transaction tx1 = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv, BigInteger.TEN, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx1, Util.PRIVATE_KEY_MINTING.getValue());

        Transaction tx2 = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv, BigInteger.TEN, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx1, Util.PRIVATE_KEY_MINTING.getValue());

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(tx1);
        transactions.add(tx2);
        AppServiceProvider.getBootstrapService().putTransactionInBlockchain(tx1, AppServiceProvider.getSerializationService().getHashString(tx1), state);
        AppServiceProvider.getBootstrapService().putTransactionInBlockchain(tx2, AppServiceProvider.getSerializationService().getHashString(tx2), state);

        Block blk = appBlockManager.composeBlock(transactions, application);
        appBlockManager.signBlock(blk, pvkeyRecv);

        UtilTest.printAccountsWithBalance(state.getAccounts());

        TestCase.assertNotNull("Should have returned a block: ", blk);
        TestCase.assertEquals("Should have been 1 account", 1, getValidAccounts(state.getAccounts()));
        TestCase.assertEquals(Util.VALUE_MINTING, getBalance(Util.PUBLIC_KEY_MINTING));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(Util.PUBLIC_KEY_MINTING));

        ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

        TestCase.assertTrue("Should have executed!", executionReport.isOk());
        TestCase.assertEquals(Util.VALUE_MINTING.subtract(BigInteger.TEN), getBalance(Util.PUBLIC_KEY_MINTING));
        TestCase.assertEquals(BigInteger.ONE, getNonce(Util.PUBLIC_KEY_MINTING));

        TestCase.assertEquals(BigInteger.TEN, getBalance(pbkeyRecv));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(pbkeyRecv));

        UtilTest.printAccountsWithBalance(state.getAccounts());
    }

    @Test
    public void testProposeAndExecuteBlock2BadTx3OK() throws Exception{
        state.setAccounts(new Accounts(accountsContext));

        PrivateKey pvkeyRecv1 = new PrivateKey("RECV1");
        PublicKey pbkeyRecv1 = new PublicKey(pvkeyRecv1);

        PrivateKey pvkeyRecv2 = new PrivateKey("RECV2");
        PublicKey pbkeyRecv2 = new PublicKey(pvkeyRecv2);

        List<Transaction> transactions = new ArrayList<>();
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (nonce mismatch)
        //transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN.pow(100), BigInteger.ONE));
        //valid transaction to recv2
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv2, BigInteger.TEN, BigInteger.ONE));
        //not valid transaction to recv2 (nonce mismatch)
        //transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv2, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv2 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv2, BigInteger.TEN.pow(100), BigInteger.ZERO));
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN, BigInteger.valueOf(2)));

        for (Transaction transaction:transactions){
            AppServiceProvider.getTransactionService().signTransaction(transaction, Util.PRIVATE_KEY_MINTING.getValue());

            AppServiceProvider.getBootstrapService().putTransactionInBlockchain(transaction, AppServiceProvider.getSerializationService().getHashString(transaction), state);
        }

        Block blk = appBlockManager.composeBlock(transactions, application);
        appBlockManager.signBlock(blk, pvkeyRecv1);

        UtilTest.printAccountsWithBalance(state.getAccounts());

        TestCase.assertNotNull("Should have returned a block: ", blk);
        TestCase.assertEquals("Should have been 1 account", 1, getValidAccounts(state.getAccounts()));
        TestCase.assertEquals(Util.VALUE_MINTING, getBalance(Util.PUBLIC_KEY_MINTING));
        TestCase.assertEquals(BigInteger.ZERO, getNonce(Util.PUBLIC_KEY_MINTING));

        ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

        TestCase.assertTrue("Should have executed!", executionReport.isOk());

        TestCase.assertEquals(Util.VALUE_MINTING.subtract(BigInteger.valueOf(30)), getBalance(Util.PUBLIC_KEY_MINTING));
        TestCase.assertEquals(BigInteger.valueOf(3), getNonce(Util.PUBLIC_KEY_MINTING));

        TestCase.assertEquals(BigInteger.valueOf(20), getBalance(pbkeyRecv1));
        TestCase.assertEquals(BigInteger.valueOf(0), getNonce(pbkeyRecv1));

        TestCase.assertEquals(BigInteger.valueOf(10), getBalance(pbkeyRecv2));
        TestCase.assertEquals(BigInteger.valueOf(0), getNonce(pbkeyRecv2));


        UtilTest.printAccountsWithBalance(state.getAccounts());
    }

    @Test
    public void validateBlock() throws Exception{
        state.setAccounts(new Accounts(accountsContext));

        PrivateKey pvkeyRecv1 = new PrivateKey("RECV1");
        PublicKey pbkeyRecv1 = new PublicKey(pvkeyRecv1);

        PrivateKey pvkeyRecv2 = new PrivateKey("RECV2");
        PublicKey pbkeyRecv2 = new PublicKey(pvkeyRecv2);

        List<Transaction> transactions = new ArrayList<>();
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (nonce mismatch)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv1 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN.pow(100), BigInteger.ONE));
        //valid transaction to recv2
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv2, BigInteger.TEN, BigInteger.ONE));
        //not valid transaction to recv2 (nonce mismatch)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv2, BigInteger.TEN, BigInteger.ZERO));
        //not valid transaction to recv2 (not enough funds)
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv2, BigInteger.TEN.pow(100), BigInteger.ZERO));
        //valid transaction to recv1
        transactions.add(AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, pbkeyRecv1, BigInteger.TEN, BigInteger.valueOf(2)));

        for (Transaction transaction:transactions){
            AppServiceProvider.getTransactionService().signTransaction(transaction, Util.PRIVATE_KEY_MINTING.getValue());

            AppServiceProvider.getBootstrapService().putTransactionInBlockchain(transaction, AppServiceProvider.getSerializationService().getHashString(transaction), state);
        }

        Block block = appBlockManager.composeBlock(transactions, application);
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
        for (int i = 0; i < signersStringList.size(); i++){
            signers.add(Util.hexStringToByteArray(signersStringList.get(i)));
        }

        TestCase.assertTrue(AppServiceProvider.getMultiSignatureService().verifyAggregatedSignature(signers, signature, commitment, message, bitmap));

        //test tamper block
        byte[] buff = block.getPrevBlockHash();
        if (buff.length > 0){
            buff[0] = 'J';
        }
        block.setPrevBlockHash(buff);
        message = AppServiceProvider.getSerializationService().getHash(block);

        TestCase.assertFalse(AppServiceProvider.getMultiSignatureService().verifyAggregatedSignature(signers, signature, commitment, message, bitmap));
    }

    private int getValidAccounts(Accounts accounts) throws Exception{
        int counter = 0;

        for (AccountAddress accountAddress : accounts.getAddresses()){
            if (AppServiceProvider.getAccountStateService().getAccountState(accountAddress, accounts) != null){
                counter++;
            }
        }

        return (counter);
    }

    private BigInteger getBalance(PublicKey pbKey) throws Exception{
        if ((state == null) || (pbKey == null)){
            return(Util.BIG_INT_MIN_ONE);
        }

        AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(new AccountAddress(pbKey.getValue()), state.getAccounts());

        if (accountState == null){
            return (Util.BIG_INT_MIN_ONE);
        }

        return(accountState.getBalance());
    }

    private BigInteger getNonce(PublicKey pbKey) throws Exception{
        if ((state == null) || (pbKey == null)){
            return(Util.BIG_INT_MIN_ONE);
        }

        AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(new AccountAddress(pbKey.getValue()), state.getAccounts());

        if (accountState == null){
            return (Util.BIG_INT_MIN_ONE);
        }

        return(accountState.getNonce());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComposeBlockWithNullTransactionListShouldThrowException(){
        Block block = appBlockManager.composeBlock(null, application);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComposeBlockWithNullApplicationShouldThrowException(){
        Block block = appBlockManager.composeBlock(Arrays.asList(), null);
    }

    @Test
    public void testComposeBlockWithZeroTransaction(){
        Block block = appBlockManager.composeBlock(Arrays.asList(), application);
        Assert.assertTrue("Block cannot be null", block!=null);
        Assert.assertTrue("PrevBlockHash cannot be null", block.prevBlockHash!=null);
        Assert.assertTrue("ListOfTxHashes does not have exactly 0 hash", block.getListTXHashes().size() == 0);
    }

    @Test
    public void testComposeBlockWithOneValidTransaction(){
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue());
        Block block = appBlockManager.composeBlock(Arrays.asList(tx), application);
        Assert.assertTrue("ListOfTxHashes does not have exactly 1 hash", block.getListTXHashes().size() == 1);
    }

    @Test
    public void testComposeBlockWithOneNotSignedTransaction(){
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN.pow(100), BigInteger.ZERO);
        //AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue());
        Block block = appBlockManager.composeBlock(Arrays.asList(tx), application);
        Assert.assertTrue("ListOfTxHashes does not have exactly 0 hash", block.getListTXHashes().size() == 0);
    }

    @Test
    public void testComposeBlockWithOneNotEnoughFundsTransaction(){
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN.pow(100), BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue());
        Block block = appBlockManager.composeBlock(Arrays.asList(tx), application);
        Assert.assertTrue("ListOfTxHashes does not have exactly 0 hash", block.getListTXHashes().size() == 0);
    }

    //TODO: Readd when NonceIsVerified
    //@Test
    public void testComposeBlockWithOneNonceMismatchTransaction(){
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN, BigInteger.TEN);
        AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue());
        Block block = appBlockManager.composeBlock(Arrays.asList(tx), application);
        Assert.assertTrue("ListOfTxHashes does not have exactly 0 hash", block.getListTXHashes().size() == 0);
    }

    @Test
    public void testComposeBlockWithOneValidAndOneInValidTransaction(){
        Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN.pow(100), BigInteger.ZERO);
        Transaction tx2 = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING, publicKey, BigInteger.TEN.pow(1), BigInteger.ZERO);

        AppServiceProvider.getTransactionService().signTransaction(tx, Util.PRIVATE_KEY_MINTING.getValue());
        AppServiceProvider.getTransactionService().signTransaction(tx2, Util.PRIVATE_KEY_MINTING.getValue());

        Block block = appBlockManager.composeBlock(Arrays.asList(tx, tx2), application);
        Assert.assertTrue("ListOfTxHashes does not have exactly 1 hash", block.getListTXHashes().size() == 1);
    }

    @Test
    public void testComposeBlockWithNoCurrentBlockFoundTransaction(){
        Block block = appBlockManager.composeBlock(Arrays.asList(), application);
        Assert.assertTrue("Block cannot be null", block!=null);
        Assert.assertTrue("PrevBlockHash cannot be null", block.prevBlockHash!=null);
        Assert.assertTrue("ListOfTxHashes does not have exactly 0 hash", block.getListTXHashes().size() == 0);
    }

    @Test
    public void testSignEmptyBlock(){
        Block block = appBlockManager.composeBlock(Arrays.asList(), application);
        appBlockManager.signBlock(block, privateKey);

        Assert.assertTrue("Commitment cannot be null", block.getCommitment() != null);
        Assert.assertTrue("Signature cannot be null", block.getSignature() != null);
        Assert.assertTrue("Signature cannot be null", block.getListPublicKeys() != null);
    }

    @Test
    public void testVerifySignEmptyBlock(){
        Block block = appBlockManager.composeBlock(Arrays.asList(), application);
        appBlockManager.signBlock(block, privateKey);
        MultiSignatureService multiSignatureService = AppServiceProvider.getMultiSignatureService();

        Assert.assertTrue("Commitment cannot be null", block.getCommitment() != null);
        Assert.assertTrue("Signature cannot be null", block.getSignature() != null);
        Assert.assertTrue("Signature cannot be null", block.getListPublicKeys() != null);
    }

}
