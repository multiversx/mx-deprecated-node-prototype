package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.benchmark.BenchmarkResult;
import network.elrond.benchmark.MultipleTransactionResult;
import network.elrond.blockchain.Blockchain;
import network.elrond.crypto.PKSKPair;
import network.elrond.data.Block;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;
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
     * Send multiple transactions of value to account
     *
     * @param receiver
     * @param value
     * @param nrTransactions
     * @param application
     * @return result with successfultransactions and failedtransactions number
     */
    MultipleTransactionResult sendMultipleTransactions(AccountAddress receiver, BigInteger value, Integer nrTransactions, Application application);

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

    PKSKPair generatePublicKeyAndPrivateKey(String strPrivateKey);

    /**
     * Get BenchmarkResult
     *
     * @param benchmarkId
     * @param application
     * @return
     */
    List<BenchmarkResult> getBenchmarkResult(String benchmarkId, Application application);

    Transaction getTransactionFromHash(String transactionHash, Blockchain blockchain);

    Block getBlockFromHash(String blockHash, Blockchain blockchain);
}
