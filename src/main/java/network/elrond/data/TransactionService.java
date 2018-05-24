package network.elrond.data;

public interface TransactionService {

    //byte[] getHash(Transaction tx, boolean withSig);

    void signTransaction(Transaction tx, byte[] privateKeysBytes);

    boolean verifyTransaction(Transaction tx);
}
