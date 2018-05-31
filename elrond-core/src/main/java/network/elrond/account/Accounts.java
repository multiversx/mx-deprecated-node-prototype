package network.elrond.account;

import network.elrond.service.AppServiceProvider;

import network.elrond.blockchain.PersistenceUnitContainer;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Accounts implements Serializable, PersistenceUnitContainer {


    private final AccountsContext context;

    private final AccountsPersistenceUnit<AccountAddress, AccountState> unit;

    private final Set<AccountAddress> addresses;

    public Accounts(AccountsContext context) throws IOException {
        this.context = context;
        this.unit = new AccountsPersistenceUnit<>(context.getDatabasePath());
        addresses = new HashSet<>();
        AppServiceProvider.getAccountStateService().initialMintingToKnownAddress(this);
    }

    public AccountsPersistenceUnit<AccountAddress, AccountState> getAccountsPersistenceUnit() {
        return unit;
    }

    public void flush() {
        unit.getCache().clear();
    }

    @Override
    public void stopPersistenceUnit() {
        try {
            unit.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<AccountAddress> getAddresses(){
        return(Collections.synchronizedSet(addresses));
    }
}
