package network.elrond.account;

import java.io.IOException;
import java.io.Serializable;

public class Accounts implements Serializable {

    private final AccountsContext context;

    private final AccountsPersistenceUnit<AccountAddress, AccountState> accounts;

    public Accounts(AccountsContext context) throws IOException {
        this.context = context;
        this.accounts = new AccountsPersistenceUnit<>(context.getDatabasePath());
    }

    public AccountsPersistenceUnit<AccountAddress, AccountState> getAccountsPersistenceUnit() {
        return accounts;
    }
}
