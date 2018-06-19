package network.elrond.account;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;

import network.elrond.blockchain.PersistenceUnitContainer;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Accounts implements Serializable, PersistenceUnitContainer {


    private final AccountsContext context;

    private final AccountsPersistenceUnit<AccountAddress, AccountState> unit;

    private final Set<AccountAddress> addresses;

    public Accounts(AccountsContext context, AccountsPersistenceUnit<AccountAddress, AccountState> unit) throws IOException {
        Util.check(context!=null, "context!=null");
        Util.check(unit!=null, "unit!=null");
        this.context = context;
        this.unit = unit;
        addresses = new HashSet<>();
        AppServiceProvider.getAccountStateService().initialMintingToKnownAddress(this);
    }

    public AccountsPersistenceUnit<AccountAddress, AccountState> getAccountsPersistenceUnit() {
        return unit;
    }

    public Set<AccountAddress> getAddresses() {
        return (Collections.synchronizedSet(addresses));
    }

    public void flush() {
        unit.clear();
    }

    @Override
    public void stopPersistenceUnit() {
        try {
            unit.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
