package network.elrond.account;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

public class AccountStateServiceImplTest {

    private AccountStateService accountStateService;
    private Accounts accounts;
    private AccountAddress address;

    @Before
    public void SetUp() throws IOException {
        accountStateService = new AccountStateServiceImpl();
        AccountsContext context = new AccountsContext();
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        context.setShard(AppServiceProvider.getShardingService().getShard(publicKeyMinting.getValue()));
        accounts = new Accounts(context, new AccountsPersistenceUnit<>(""));
        address = AccountAddress.fromHexString("testAddresss");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetOrCreateaAccountStateWithNullAddressShouldThrowException() throws IOException, ClassNotFoundException {
        accountStateService.getOrCreateAccountState(null, accounts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetOrCreateaAccountStateWithNullAccountsShouldThrowException() throws IOException, ClassNotFoundException {
        accountStateService.getOrCreateAccountState(address, null);
    }

    @Test
    public void testGetOrCreateaAccountStateWithExistingAddress() throws IOException, ClassNotFoundException {
        AccountState initialState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        initialState.setNonce(BigInteger.TEN);
        accountStateService.setAccountState(address, initialState, accounts);
        AccountState state = accountStateService.getOrCreateAccountState(address, accounts);
        Assert.assertEquals(initialState.getNonce(), state.getNonce());
    }

    @Test
    public void testGetOrCreateaAccountStateWithNonExistingAddress() throws IOException, ClassNotFoundException {
        AccountState state = accountStateService.getOrCreateAccountState(address, accounts);
        Assert.assertNotNull(state);
    }

    @Test
    public void testGetAccountStateWithExistingAddress() throws IOException, ClassNotFoundException {
        AccountState initialState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        initialState.setNonce(BigInteger.TEN);
        accountStateService.setAccountState(address, initialState, accounts);
        AccountState state = accountStateService.getOrCreateAccountState(address, accounts);
        Assert.assertNotNull(state);
    }

    @Test
    public void testGetAccountStateWithNonExistingAddress() throws IOException, ClassNotFoundException {
        AccountState state = accountStateService.getAccountState(address, accounts);
        Assert.assertNull(state);
    }

    @Test
    public void testRollbackAccountWithExistingAddress() throws IOException, ClassNotFoundException {
        AccountState initialState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        initialState.setNonce(BigInteger.TEN);
        accountStateService.setAccountState(address, initialState, accounts);
        AccountState state = accountStateService.getAccountState(address, accounts);
        Assert.assertNotNull(state);
        accountStateService.rollbackAccountStates(accounts);
        state = accountStateService.getAccountState(address, accounts);
        Assert.assertNull(state);
    }

    @Test
    public void testCommitAccountWithExistingAddress() throws IOException, ClassNotFoundException {
        AccountState initialState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        initialState.setNonce(BigInteger.ONE);
        initialState.setBalance(BigInteger.TEN);
        accountStateService.setAccountState(address, initialState, accounts);
        AccountState state = accountStateService.getAccountState(address, accounts);
        Assert.assertNotNull(state);
        accountStateService.commitAccountStates(accounts);
        state = accountStateService.getAccountState(address, accounts);
        Assert.assertNotNull(state);
        accountStateService.rollbackAccountStates(accounts);
        state = accountStateService.getAccountState(address, accounts);
        Assert.assertNotNull(state);
        Assert.assertTrue(state.getBalance().compareTo(BigInteger.TEN) == 0);
        Assert.assertTrue(state.getNonce().compareTo(BigInteger.ONE) == 0);
    }

    @Test
    public void convertAccountStateToRLPAndBackAgain() {
        AccountState initialState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        initialState.setNonce(BigInteger.ONE);
        initialState.setBalance(BigInteger.TEN);
        byte[] rlp = accountStateService.convertAccountStateToRLP(initialState);
        AccountState back = accountStateService.convertToAccountStateFromRLP(rlp);
        Assert.assertEquals(BigInteger.ONE, back.getNonce());
        Assert.assertEquals(BigInteger.TEN, back.getBalance());
    }

    @Test
    public void convertAccountStateToRLPAndBackAgainWithOneZero() {
        AccountState initialState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        byte[] rlp = accountStateService.convertAccountStateToRLP(initialState);
        AccountState back = accountStateService.convertToAccountStateFromRLP(rlp);
        Assert.assertEquals(BigInteger.ZERO, back.getNonce());
        Assert.assertEquals(BigInteger.ZERO, back.getBalance());
    }

    @Test
    public void testInitialMintingToKnownAddress() throws IOException, ClassNotFoundException {
        accountStateService.initialMintingToKnownAddress(accounts);
        AccountState state = accountStateService.getAccountState(AppServiceProvider.getShardingService().getAddressForMinting(accounts.getShard()), accounts);
        Assert.assertNotNull(state);
        Assert.assertTrue(state.getBalance().compareTo(Util.VALUE_MINTING) == 0);
    }


    @Test
    public void testGenerateGenesisBlock() throws IOException, ClassNotFoundException {
        AccountsContext accountsContext = new AccountsContext();
        AppState appState = new AppState();
        AppContext appContext = new AppContext();
        PrivateKey privateKey = new PrivateKey();
        PublicKey publicKey = new PublicKey(new PrivateKey());
        appContext.setPrivateKey(privateKey);
        Shard shard = AppServiceProvider.getShardingService().getShard(publicKey.getValue());
        accountsContext.setShard(shard);
        appState.setShard(shard);

        accounts = new Accounts(accountsContext, new AccountsPersistenceUnit<>(""));
        String initialAddress = Util.byteArrayToHexString(publicKey.getValue());

        accountStateService.generateGenesisBlock(initialAddress, BigInteger.TEN, appState, appContext);
        AccountState state = accountStateService.getAccountState(AppServiceProvider.getShardingService().getAddressForMinting(accounts.getShard()), accounts);
        Assert.assertNotNull(state);
        Assert.assertTrue(state.getBalance().compareTo(Util.VALUE_MINTING) == 0);
        Assert.assertTrue(state.getBalance().compareTo(Util.VALUE_MINTING) == 0);
    }

//    public Fun.Tuple2<Block, Transaction> generateGenesisBlock(String initialAddress, BigInteger initialValue,
//                                                               AccountsContext accountsContextTemporary, PrivateKey privateKey) {
//
//        if (initialValue.compareTo(Util.VALUE_MINTING) > 0) {
//            initialValue = Util.VALUE_MINTING;
//        }
//
//
//        Transaction transactionMint = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING,
//                new PublicKey(Util.hexStringToByteArray(initialAddress)), initialValue, BigInteger.ZERO);
//        AppServiceProvider.getTransactionService().signTransaction(transactionMint, Util.PRIVATE_KEY_MINTING.getValue(), Util.PUBLIC_KEY_MINTING.getValue());
//
//        Block genesisBlock = new Block();
//        genesisBlock.setNonce(BigInteger.ZERO);
//        genesisBlock.getListTXHashes().add(AppServiceProvider.getSerializationService().getHash(transactionMint));
//
//        //compute state root hash
//        try {
//            Accounts accountsTemp = new Accounts(accountsContextTemporary, new AccountsPersistenceUnit<>(accountsContextTemporary.getDatabasePath()));
//
//            ExecutionService executionService = AppServiceProvider.getExecutionService();
//            ExecutionReport executionReport = executionService.processTransaction(transactionMint, accountsTemp);
//            if (!executionReport.isOk()) {
//                return (null);
//            }
//            genesisBlock.setAppStateHash(accountsTemp.getAccountsPersistenceUnit().getRootHash());
//            AppBlockManager.instance().signBlock(genesisBlock, privateKey);
//            accountsTemp.getAccountsPersistenceUnit().close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return (null);
//        }
//
//        return (new Fun.Tuple2<>(genesisBlock, transactionMint));
//    }

}
