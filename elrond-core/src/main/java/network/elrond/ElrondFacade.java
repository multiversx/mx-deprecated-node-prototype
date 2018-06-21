package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.crypto.PKSKPair;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;
import network.elrond.p2p.PingResponse;
import org.mapdb.Fun;

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
     * Get transaction receipt
     *
     * @param transactionHash
     * @param application
     * @return
     */
    Receipt getReceipt(String transactionHash, Application application);

    /**
     * Send value to account
     *
     * @param receiver
     * @param value
     * @param application
     * @return the transaction hash
     */
    Transaction send(AccountAddress receiver, BigInteger value, Application application);

    /**
     * Pings an IP address and checks if port is open
     *
     * @param ipAddress
     * @param port
     * @return
     */
    PingResponse ping(String ipAddress, int port);

    /**
     * Generate public key and private key
     *
     * @return
     */
    PKSKPair generatePublicKeyAndPrivateKey();
    /**
     * Generate public key, private key and calculate shard placement
     *
     * @param privateKey
     * @return
     */

    Fun.Tuple2<PKSKPair, Integer> generatePublicKeyPrivateKeyShardNr(String strPrivateKey);

    /**
     * Generate public key from a private key
     *
     * @param privateKey
     * @return
     */
    PKSKPair generatePublicKeyFromPrivateKey(String privateKey);
}
