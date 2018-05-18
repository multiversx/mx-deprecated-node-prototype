package network.elrond.data;

import network.elrond.application.AppState;

public interface TransactionService {
    String encodeJSON(Transaction tx, boolean withSig);
    Transaction decodeJSON(String strJSONData);
    byte[] getHash(Transaction tx, boolean withSig);
    String getHashAsString(Transaction tx, boolean withSig);
    void signTransaction(Transaction tx, byte[] privateKeysBytes);
    boolean verifyTransaction(Transaction tx);
    Transaction fetchTransaction(String strHash, AppState appState);
}
