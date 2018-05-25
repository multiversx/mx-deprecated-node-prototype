package network.elrond.data;

import network.elrond.blockchain.Blockchain;

import java.io.IOException;
import java.util.List;

public interface TransactionService {

    void signTransaction(Transaction tx, byte[] privateKeysBytes);

    boolean verifyTransaction(Transaction tx);

    List<Transaction> getTransactions(Blockchain blockchain, Block block) throws IOException, ClassNotFoundException;
}
