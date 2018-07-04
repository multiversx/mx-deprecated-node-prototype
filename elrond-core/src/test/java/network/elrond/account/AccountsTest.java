package network.elrond.account;

import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class AccountsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testAccountsFromNullContextShouldThrowException() throws IOException {
        Accounts accounts  = new Accounts(null, new AccountsPersistenceUnit<>(""));
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountsFromNullAccountsPersistenceUnitShouldThrowException() throws IOException {
        Accounts accounts  = new Accounts(new AccountsContext(), null);
        Assert.fail();
    }

    @Test
    public void testGetAccountsPersistenceUnit() throws IOException {
        AccountsPersistenceUnit<AccountAddress, AccountState> unit = new AccountsPersistenceUnit<>("");
        AccountsContext accountsContext = new AccountsContext();
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        accountsContext.setShard(AppServiceProvider.getShardingService().getShard(publicKeyMinting.getValue()));
        Accounts accounts  = new Accounts(accountsContext, unit);
        Assert.assertEquals(unit, accounts.getAccountsPersistenceUnit());
    }

    @Test
    public void testGetAddresses() throws IOException {
        AccountsPersistenceUnit<AccountAddress, AccountState> unit = new AccountsPersistenceUnit<>("");
        AccountsContext accountsContext = new AccountsContext();
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        accountsContext.setShard(AppServiceProvider.getShardingService().getShard(publicKeyMinting.getValue()));
        Accounts accounts  = new Accounts(accountsContext, unit);
        Assert.assertTrue(accounts.getAddresses()!=null );
        Assert.assertEquals(1, accounts.getAddresses().size());
    }
}
