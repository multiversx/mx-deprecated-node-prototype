package network.elrond.data;

public interface TransactionService {
    String encodeJSON(Transaction tx, boolean withSig);
    Transaction decodeJSON(String strJSONData);
    byte[] getHash(Transaction tx, boolean withSig);
    void signTransaction(Transaction tx, byte[] privateKeysBytes);
    boolean verifyTransaction(Transaction tx);
}
