package network.elrond.account;

import org.junit.Assert;
import org.junit.Test;
import java.math.BigInteger;

public class AccountStateTest {

    @Test
    public void testAccountStateDefaultConstructor() {
        AccountState accountState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        Assert.assertEquals(BigInteger.ZERO, accountState.getBalance());
        Assert.assertEquals(BigInteger.ZERO, accountState.getNonce());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateConstructorWithNegativeBalanceShouldThrowException() {
        new AccountState(BigInteger.ZERO, BigInteger.valueOf(-1), AccountAddress.EMPTY_ADDRESS);
        Assert.fail();
    }

    @Test
    public void testAccountStateConstructorWithCorrectBalance() {
        AccountState accountState = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2), AccountAddress.EMPTY_ADDRESS);
        Assert.assertTrue(accountState.getBalance().longValue() == 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateConstructorWithNegativeNonceShouldThrowException() {
        new AccountState(BigInteger.valueOf(-1), BigInteger.valueOf(2), AccountAddress.EMPTY_ADDRESS);
        Assert.fail();
    }

    @Test
    public void testAccountStateConstructorWithCorrectNonce() {
        AccountState accountState = new AccountState(BigInteger.valueOf(1), BigInteger.valueOf(2), AccountAddress.EMPTY_ADDRESS);
        Assert.assertTrue(accountState.getNonce().longValue() == 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateCopyConstructorWithNullShouldThrowException() {
        new AccountState((AccountState) null);
        Assert.fail();
    }

    @Test
    public void testAccountStateCopyConstructorWithCorrectValues() {
        AccountState accountState = new AccountState(BigInteger.valueOf(1), BigInteger.valueOf(2), AccountAddress.EMPTY_ADDRESS);
        AccountState copiedState = new AccountState(accountState);

        Assert.assertTrue(copiedState.getBalance().longValue() == 2);
        Assert.assertTrue(copiedState.getNonce().longValue() == 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateSetNonceNegativeShouldThrowException() {
        AccountState accountState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        accountState.setNonce(BigInteger.valueOf(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateSetNonceNullShouldThrowException() {
        AccountState accountState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        accountState.setNonce(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateSetBalanceNegativeShouldThrowException() {
        AccountState accountState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        accountState.setBalance(BigInteger.valueOf(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateSetBalanceNullShouldThrowException() {
        AccountState accountState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        accountState.setBalance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountStateAddToBalanceNullShouldThrowException() {
        AccountState accountState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        accountState.addToBalance(null);
    }
}
