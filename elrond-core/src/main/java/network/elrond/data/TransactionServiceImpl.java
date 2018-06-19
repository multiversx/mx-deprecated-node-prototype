package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.crypto.Signature;
import network.elrond.crypto.SignatureService;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SerializationService serializationService = AppServiceProvider.getSerializationService();


    /**
     * Computes the hash of the complete tx info
     * Used as a mean of tx identification
     *
     * @param tx      transaction
     * @param withSig whether or not to include the signature parts in hash
     * @return hash as byte array
     */
//    public byte[] getHash(Transaction tx, boolean withSig) {
//        String json = AppServiceProvider.getSerializationService().encodeJSON(tx);
//        return (Util.SHA3.get().digest(json.getBytes()));
//    }

    /**
     * Signs the transaction using private keys
     *
     * @param tx               transaction
     * @param privateKeysBytes private key as byte array
     */
    public void signTransaction(Transaction tx, byte[] privateKeysBytes, byte[] publicKeyBytes) {

        if (tx == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        if (privateKeysBytes == null) {
            throw new IllegalArgumentException("PrivateKeysBytes cannot be null");
        }


        tx.setSignature(null);
        tx.setChallenge(null);

        byte[] hashNoSigLocal = serializationService.getHash(tx);

//        tx.setSignature(signature);
//        tx.setChallenge(challenge);


        Signature sig;

        SignatureService schnorr = AppServiceProvider.getSignatureService();
        sig = schnorr.signMessage(hashNoSigLocal, privateKeysBytes, publicKeyBytes);

        tx.setSignature(sig.getSignature());
        tx.setChallenge(sig.getChallenge());
        tx.setPubKey(Util.byteArrayToHexString(publicKeyBytes));
    }

    /**
     * Verify the data stored in tx
     *
     * @param tx to be verified
     * @return true if tx passes all consistency tests
     */
    public boolean verifyTransaction(Transaction tx) {
        if (tx == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        //test 1. consistency checks
        if ((tx.getNonce().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getValue().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getSignature() == null) ||
                (tx.getChallenge() == null) ||
                (tx.getSignature().length == 0) ||
                (tx.getChallenge().length == 0) ||
                (tx.getSendAddress().length() != Util.MAX_LEN_ADDR * 2) ||
                (tx.getReceiverAddress().length() != Util.MAX_LEN_ADDR * 2) ||
                (tx.getPubKey().length() != Util.MAX_LEN_PUB_KEY * 2)
                ) {
            return (false);
        }

        //test 2. verify if sender address is generated from public key used to sign tx
        if (!tx.getSendAddress().equals(Util.getAddressFromPublicKey(Util.hexStringToByteArray(tx.getPubKey())))) {
            return (false);
        }

        //test 3. verify the signature
        byte[] signature = tx.getSignature();
        byte[] challenge = tx.getChallenge();

        tx.setSignature(null);
        tx.setChallenge(null);

        byte[] message = serializationService.getHash(tx);

        tx.setSignature(signature);
        tx.setChallenge(challenge);

        SignatureService schnorr = AppServiceProvider.getSignatureService();

        return schnorr.verifySignature(tx.getSignature(), tx.getChallenge(), message, Util.hexStringToByteArray(tx.getPubKey()));
    }

    @Override
    public List<Transaction> getTransactions(Blockchain blockchain, Block block) throws IOException, ClassNotFoundException {

        if (blockchain == null) {
            throw new IllegalArgumentException("Blockchain cannot be null");
        }

        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }

        List<Transaction> transactions = new ArrayList<>();

        //JLS 2018.05.29 - need to store fetched transaction!
        //BlockchainService appPersistenceService = AppServiceProvider.getAppPersistanceService();

        List<byte[]> hashes = block.getListTXHashes();
        for (byte[] hash : hashes) {
            String hashString = Util.getDataEncoded64(hash);
            Transaction transaction = AppServiceProvider.getBlockchainService().get(hashString, blockchain, BlockchainUnitType.TRANSACTION);
            if (transaction == null) {
                logger.info("Found null transaction for hash: " + hash);
                continue;
            }
            transactions.add(transaction);
            //appPersistenceService.put(hashString, transaction, blockchain, BlockchainUnitType.TRANSACTION);
        }

        return transactions;
    }

    @Override
    public Transaction generateTransaction(PublicKey sender, PublicKey receiver, long value, long nonce) {
        return generateTransaction(sender, receiver, BigInteger.valueOf(value), BigInteger.valueOf(nonce));
    }

    @Override
    public Transaction generateTransaction(PublicKey sender, PublicKey receiver, BigInteger value, BigInteger nonce) {
        Transaction t = new Transaction(Util.getAddressFromPublicKey(sender.getValue()),
                Util.getAddressFromPublicKey(receiver.getValue()),
                value,
                nonce);
        t.setPubKey(Util.getAddressFromPublicKey(sender.getValue()));


        return t;
    }


}