package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.crypto.PublicKey;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface TransactionService {

    void signTransaction(Transaction tx, byte[] privateKeysBytes, byte[] publicKeysBytes);

    boolean verifyTransaction(Transaction tx);

    List<Transaction> getTransactions(Blockchain blockchain, Block block) throws IOException, ClassNotFoundException;

    Transaction generateTransaction(PublicKey sender, PublicKey receiver, long value, long nonce);

    Transaction generateTransaction(PublicKey sender, PublicKey receiver, BigInteger value, BigInteger nonce);
}
