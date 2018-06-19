package network.elrond.account;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;

import network.elrond.blockchain.PersistenceUnitContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Accounts implements Serializable, PersistenceUnitContainer {

    private final AccountsContext context;

    private final AccountsPersistenceUnit<AccountAddress, AccountState> unit;

    private final Set<AccountAddress> addresses;
    private static final Logger logger = LogManager.getLogger(Accounts.class);

    public Accounts(AccountsContext context, AccountsPersistenceUnit<AccountAddress, AccountState> unit) throws IOException {
        logger.traceEntry();
        Util.check(context!=null, "context!=null");
        Util.check(unit!=null, "unit!=null");
        this.context = context;
        this.unit = unit;
        addresses = new HashSet<>();
        AppServiceProvider.getAccountStateService().initialMintingToKnownAddress(this);
        logger.traceExit();
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
        logger.traceEntry();
        try {
            unit.close();
        } catch (IOException e) {
            logger.catching(e);
        }
        logger.traceExit();
    }
}
