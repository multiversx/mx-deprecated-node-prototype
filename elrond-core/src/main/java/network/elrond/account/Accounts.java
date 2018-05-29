package network.elrond.account;

import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.io.Serializable;

public class Accounts implements Serializable {


    private final AccountsContext context;

    private final AccountsPersistenceUnit<AccountAddress, AccountState> unit;

    public Accounts(AccountsContext context) throws IOException {
        this.context = context;
        this.unit = new AccountsPersistenceUnit<>(context.getDatabasePath());
        AppServiceProvider.getAccountStateService().initialMintingToKnownAddress(this);
    }

    public AccountsPersistenceUnit<AccountAddress, AccountState> getAccountsPersistenceUnit() {
        return unit;
    }

    public void flush() {
        unit.getCache().clear();
    }
}
