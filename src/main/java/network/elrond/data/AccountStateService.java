package network.elrond.data;

public interface AccountStateService {
    String encodeJSON(AccountState accountState);
    AccountState decodeJSON(String strJSONData);
    byte[] getHash(AccountState accountState);
    AccountState[] executeTransaction(Transaction tx) throws Exception;
    AccountState getCreateAccount(String strAdress);
    void updateAccount(String strAdress, AccountState accountState);
    void doRollBackLastAccumulatedData();
    void doCommitLastAccumulatedData();
    void executeTransactionAccumulatingData(Transaction tx) throws Exception;
}
