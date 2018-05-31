package network.elrond;

import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;

import java.math.BigInteger;

public interface ElrondFacade {

    /**
     * Start Elrond node
     *
     * @param context
     * @return
     */
    Application start(AppContext context);

    /**
     * Stop Elrond node
     *
     * @param application
     * @return
     */
    boolean stop(Application application);

    /**
     * Get balance for account
     *
     * @param address
     * @param application
     * @return
     */
    BigInteger getBalance(AccountAddress address, Application application);

    /**
     * Send value to account
     *
     * @param receiver
     * @param value
     * @param application
     * @return
     */
    boolean send(AccountAddress receiver, BigInteger value, Application application);
}
