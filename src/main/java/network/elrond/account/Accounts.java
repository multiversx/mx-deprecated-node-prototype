package network.elrond.account;

import java.io.IOException;
import java.io.Serializable;

public class Accounts<A> implements Serializable {

    private final AccountsContext context;

    private final AccountsPersistenceUnit<A, AccountState> accounts;

    public Accounts(AccountsContext context) throws IOException {
        this.context = context;
        this.accounts = new AccountsPersistenceUnit<>(context.getDatabasePath());
    }

    public AccountsPersistenceUnit<A, AccountState> getAccountsPersistenceUnit() {
        return accounts;
    }
}
