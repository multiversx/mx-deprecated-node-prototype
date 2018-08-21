package network.elrond;

import net.tomp2p.peers.PeerAddress;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.blockchain.Blockchain;
import network.elrond.core.ResponseObject;

import java.math.BigInteger;
import java.util.HashSet;

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
    ResponseObject getBalance(AccountAddress address, Application application);


    /**
     * Get transaction receipt
     *
     * @param transactionHash
     * @param application
     * @return
     */
    ResponseObject getReceipt(String transactionHash, Application application);

    /**
     * Send value to account
     *
     * @param receiver
     * @param value
     * @param application
     * @return the transaction hash
     */
    ResponseObject send(AccountAddress receiver, BigInteger value, Application application);

    /**
     * Send multiple transactions of value to account
     *
     * @param receiver
     * @param value
     * @param nrTransactions
     * @param application
     * @return result with successfultransactions and failedtransactions number
     */
    ResponseObject sendMultipleTransactions(AccountAddress receiver, BigInteger value, Integer nrTransactions, Application application);

    /**
     * Send multiple transactions of value to account
     *
     * @param value
     * @param nrTransactions
     * @param application
     * @return result with successfultransactions and failedtransactions number
     */
    ResponseObject sendMultipleTransactionsToAllShards(BigInteger value, Integer nrTransactions, Application application);

    /**
     * Pings an IP address and checks if port is open
     *
     * @param ipAddress
     * @param port
     * @return
     */
    ResponseObject ping(String ipAddress, int port);

    /**
     * Pings an IP address and checks if port is closed
     *
     * @param ipAddress
     * @param port
     * @return
     */
    ResponseObject checkFreePort(String ipAddress, int port);

    /**
     * Generate public key and private key
     *
     * @return
     */

    ResponseObject generatePublicKeyAndPrivateKey(String strPrivateKey);

    /**
     * Get BenchmarkResult
     *
     * @param benchmarkId
     * @param application
     * @return
     */
    ResponseObject getBenchmarkResult(String benchmarkId, Application application);

    ResponseObject getTransactionFromHash(String transactionHash, Blockchain blockchain);

    ResponseObject getBlockFromHash(String blockHash, Blockchain blockchain);

    ResponseObject getNextPrivateKey(String requestAddress);

    ResponseObject getPrivatePublicKeyShard(Application application);

    /**
     * Return a list of peers from a given shard
     *
     * @param application
     * @param shard
     * @return
     */
    HashSet<PeerAddress> getPeersFromSelectedShard(Application application, Integer shard);
}
