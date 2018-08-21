package network.elrond.data;

import javafx.util.Pair;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.crypto.Signature;
import network.elrond.crypto.SignatureService;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The TransactionServiceImpl class implements TransactionService and is used to maintain Transaction objects
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-16
 */
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);
    private SerializationService serializationService = AppServiceProvider.getSerializationService();

    /**
     * Signs the transaction using private keys
     *
     * @param transaction      transaction
     * @param privateKeysBytes private key as byte array
     */
    public void signTransaction(Transaction transaction, byte[] privateKeysBytes, byte[] publicKeyBytes) {
        logger.traceEntry("params: {} {} {}", transaction, privateKeysBytes, publicKeyBytes);

        Util.check(transaction != null, "transaction is null");
        Util.check(privateKeysBytes != null, "privateKeysBytes is null");

        logger.trace("Setting signature data to null...");
        transaction.setSignature(null);
        transaction.setChallenge(null);

        byte[] hashNoSigLocal = serializationService.getHash(transaction);

//        tx.setSignature(signature);
//        tx.setChallenge(challenge);


        Signature sig;

        logger.trace("Signing transaction...");
        SignatureService schnorr = AppServiceProvider.getSignatureService();
        sig = schnorr.signMessage(hashNoSigLocal, privateKeysBytes, publicKeyBytes);

        transaction.setSignature(sig.getSignature());
        transaction.setChallenge(sig.getChallenge());
        transaction.setPubKey(Util.byteArrayToHexString(publicKeyBytes));

        logger.traceExit();
    }

    /**
     * Verify the data stored in tx
     *
     * @param transaction to be verified
     * @return true if tx passes all consistency tests
     */
    public boolean verifyTransaction(Transaction transaction) {
        logger.traceEntry("params: {}", transaction);

        Util.check(transaction != null, "transaction is null");

        //test 1. consistency checks
        if ((transaction.getNonce().compareTo(BigInteger.ZERO) < 0) ||
                (transaction.getValue().compareTo(BigInteger.ZERO) < 0) ||
                (transaction.getSignature() == null) ||
                (transaction.getChallenge() == null) ||
                (transaction.getSignature().length == 0) ||
                (transaction.getChallenge().length == 0) ||
                (transaction.getSenderAddress().length() != Util.MAX_LEN_ADDR * 2) ||
                (transaction.getReceiverAddress().length() != Util.MAX_LEN_ADDR * 2) ||
                (transaction.getPubKey().length() != Util.MAX_LEN_PUB_KEY * 2)
                ) {
            logger.debug("Failed at conistency check (negative nonce, negative value, sig null or empty, wrong lengths for addresses and pub key)");
            logger.debug(transaction.print().render());
            return logger.traceExit(false);
        }

        //test 2. verify if sender address is generated from public key used to sign tx
        if (!transaction.getSenderAddress().equals(Util.getAddressFromPublicKey(Util.hexStringToByteArray(transaction.getPubKey())))) {
            logger.debug("Failed at sender address not being generated (or equal) to public key");
            logger.debug(transaction.print().render());
            return (false);
        }

        //test 3. verify the signature
        Transaction localTransaction = new Transaction(transaction);

        localTransaction.setSignature(null);
        localTransaction.setChallenge(null);

        byte[] message = serializationService.getHash(localTransaction);

        SignatureService schnorr = AppServiceProvider.getSignatureService();

        boolean isSignatureVerified = schnorr.verifySignature(transaction.getSignature(), transaction.getChallenge(), message, Util.hexStringToByteArray(transaction.getPubKey()));

        if (!isSignatureVerified) {
            logger.debug("Failed at signature verify");
            logger.debug(transaction.print().render());
        }

        return logger.traceExit(isSignatureVerified);
    }

    @Override
    public List<Pair<String, Transaction>> getTransactions(Blockchain blockchain, Block block) throws IOException, ClassNotFoundException {
        logger.traceEntry("params: {} {}", blockchain, block);

        Util.check(blockchain != null, "blockchain is null");
        Util.check(block != null, "block is null");

        List<Pair<String, Transaction>> transactionHashPairs;

        //JLS 2018.05.29 - need to store fetched transaction!
        //BlockchainService appPersistenceService = AppServiceProvider.getAppPersistanceService();
        String blockHash = AppServiceProvider.getSerializationService().getHashString(block);

        List<String> hashes = BlockUtil.getTransactionsHashesAsString(block);

        transactionHashPairs = AppServiceProvider.getBlockchainService().getAll(hashes, blockchain, BlockchainUnitType.TRANSACTION);

        logger.info("Getting transactions... transactions size: {} hashes size: {}", transactionHashPairs.size(), hashes.size());
        if (transactionHashPairs.size() != hashes.size()) {
            transactionHashPairs = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK_TRANSACTIONS, false);
        }
        if (transactionHashPairs != null) {
            for (Pair<String, Transaction> transactionHashPair : transactionHashPairs) {
                String transactionHash = transactionHashPair.getKey();
                AppServiceProvider.getBlockchainService().putLocal(transactionHash, transactionHashPair.getValue(), blockchain, BlockchainUnitType.TRANSACTION);
            }
        } else {
            transactionHashPairs = new ArrayList<>();
        }
        return logger.traceExit(transactionHashPairs);
    }

    @Override
    public Transaction generateTransaction(PublicKey sender, PublicKey receiver, long value, long nonce) {
        return generateTransaction(sender, receiver, BigInteger.valueOf(value), BigInteger.valueOf(nonce));
    }

    @Override
    public Transaction generateTransaction(PublicKey sender, PublicKey receiver, BigInteger value, BigInteger nonce) {
        logger.traceEntry("params: {} {} {} {}", sender, receiver, value, nonce);

        Shard senderShard = AppServiceProvider.getShardingService().getShard(sender.getValue());
        Shard receiverShard = AppServiceProvider.getShardingService().getShard(receiver.getValue());

        Transaction t = new Transaction(Util.getAddressFromPublicKey(sender.getValue()),
                Util.getAddressFromPublicKey(receiver.getValue()),
                value,
                nonce,
                senderShard, receiverShard
        );
        t.setPubKey(Util.getAddressFromPublicKey(sender.getValue()));


        return logger.traceExit(t);
    }


}