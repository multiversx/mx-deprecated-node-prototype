package network.elrond.account;

import network.elrond.ExpectedExceptionTest;
import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import network.elrond.sharding.ShardOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

public class AccountsManagerTest extends ExpectedExceptionTest {
    AccountsManager accountsManager = null;
    Accounts accounts = null;
    ShardOperation operation = null;


    @Before
    public void SetUp() throws IOException {
        accountsManager = new AccountsManager();
        AccountsContext accountsContext = new AccountsContext();
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        accountsContext.setShard(AppServiceProvider.getShardingService().getShard(publicKeyMinting.getValue()));
        accounts = new Accounts(accountsContext, new AccountsPersistenceUnit<>(""));
        operation = ShardOperation.INTRA_SHARD;
    }

    @Test
    public void testHasFundsWithNullAccountsShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "accounts!=null");
        accountsManager.hasFunds(null, "Test", BigInteger.TEN);
    }

    @Test
    public void testHasFundsWithNullAddressShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "addressString!=null");
        accountsManager.hasFunds(accounts, null, BigInteger.TEN);
    }

    @Test
    public void testHasFunds() throws IOException, ClassNotFoundException {
        AccountAddress test = AccountAddress.fromHexString("Test");
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(test, accounts);
        senderAccountState.addToBalance(BigInteger.TEN);
        AppServiceProvider.getAccountStateService().setAccountState(test, senderAccountState, accounts);
        Assert.assertTrue(accountsManager.hasFunds(accounts, "Test", BigInteger.TEN));
        Assert.assertFalse(accountsManager.hasFunds(accounts, "Test", BigInteger.TEN.add(BigInteger.ONE)));
    }

    @Test
    public void testHasCorrectNonceWithNullAccountsShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "accounts!=null");
        accountsManager.hasCorrectNonce(null, "Test", BigInteger.TEN);
    }

    @Test
    public void testHasCorrectNonceWithNullAddressShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "addressString!=null");
        accountsManager.hasCorrectNonce(accounts, null, BigInteger.TEN);
    }

    @Test
    public void testHasCorrectNonce() throws IOException, ClassNotFoundException {
        AccountAddress test = AccountAddress.fromHexString("Test");
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(test, accounts);
        senderAccountState.setNonce(BigInteger.TEN);
        AppServiceProvider.getAccountStateService().setAccountState(test, senderAccountState, accounts);
        Assert.assertTrue(accountsManager.hasCorrectNonce(accounts, "Test", BigInteger.TEN));
        //TODO: Uncomment this line when hasCorrectNonceImplementation is ready
        //Assert.assertFalse(accountsManager.hasCorrectNonce(accounts, "Test", BigInteger.TEN.add(BigInteger.ONE)));
    }

    @Test
    public void testTransferFundsWithNullAccountsShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "accounts!=null");
        accountsManager.transferFunds(null, "Sender", "Receiver", BigInteger.TEN, BigInteger.TEN, operation);
    }

    @Test
    public void testTransferFundsWithNullSenderAddressShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "senderAddress!=null");
        accountsManager.transferFunds(accounts, null, "Receiver", BigInteger.TEN, BigInteger.TEN, operation);
    }

    @Test
    public void testTransferFundsWithNullReceiverAddressShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "receiverAddress!=null");
        accountsManager.transferFunds(accounts, "Sender", "", BigInteger.TEN, BigInteger.TEN, operation);
    }

    @Test
    public void testTransferFundsWithNegativeValueShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "value>=0");
        accountsManager.transferFunds(accounts, "Sender", "Receiver", BigInteger.valueOf(-1), BigInteger.TEN, operation);
    }

    @Test
    public void testTransferFundsWithNegativeNonceShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "nonce>=0");
        accountsManager.transferFunds(accounts, "Sender", "Receiver", BigInteger.TEN, BigInteger.valueOf(-1), operation);
    }

    @Test
    public void testTransferFunds() throws IOException, ClassNotFoundException {
        AccountAddress senderAddress = AccountAddress.fromHexString("Sender");
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(senderAddress, accounts);
        AccountAddress receiverAddress = AccountAddress.fromHexString("Receiver");
        senderAccountState.setBalance(BigInteger.TEN);
        AppServiceProvider.getAccountStateService().setAccountState(senderAddress, senderAccountState, accounts);
        accountsManager.transferFunds(accounts, "Sender", "Receiver", BigInteger.ONE, BigInteger.ZERO, operation);
        senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(senderAddress, accounts);
        Assert.assertTrue(senderAccountState.getBalance().longValue() == 9);
        AccountState receiverAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(receiverAddress, accounts);
        Assert.assertTrue(receiverAccountState.getBalance().longValue() == 1);
        Assert.assertTrue(senderAccountState.getNonce().longValue() == 1);
    }
}
