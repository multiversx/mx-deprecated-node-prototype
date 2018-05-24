package network.elrond.account;

import java.io.IOException;

public interface AccountStateService {

//    byte[] getHash(AccountState state);

    void rollbackAccountStates(Accounts accounts);

    void commitAccountStates(Accounts accounts);

    void setAccountState(String address, AccountState state, Accounts accounts) throws IOException;

    AccountState getAccountState(String address, Accounts accounts) throws IOException, ClassNotFoundException;

    AccountState getOrCreateAccountState(String address, Accounts accounts) throws IOException, ClassNotFoundException;

    byte[] getRLPencoded(AccountState state);

    AccountState getAccountStateFromRLP(byte[] rlpData);
}
