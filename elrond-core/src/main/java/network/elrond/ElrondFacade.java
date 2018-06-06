package network.elrond;

import ch.qos.logback.core.Appender;
import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.crypto.PKSKPair;
import org.mapdb.Fun;
import network.elrond.p2p.PingResponse;

import java.math.BigInteger;
import java.util.List;

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

    /**
     * Pings an IP address and checks if port is open
     * @param ipAddress
     * @param port
     * @return
     */
    PingResponse ping(String ipAddress, int port);

    /**
     * Generate public key and private key
     * @return
     */
    PKSKPair generatePublicKeyAndPrivateKey();

    /**
     * Generate public key from a private key
     * @param privateKey
     * @return
     */
    PKSKPair generatePublicKeyFromPrivateKey(String privateKey);
}
