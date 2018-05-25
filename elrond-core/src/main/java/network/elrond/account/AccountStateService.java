package network.elrond.account;

import java.io.IOException;

public interface AccountStateService {

    void rollbackAccountStates(Accounts accounts);

    void commitAccountStates(Accounts accounts);

    void setAccountState(AccountAddress address, AccountState state, Accounts accounts) throws IOException;

    AccountState getAccountState(AccountAddress address, Accounts accounts) throws IOException, ClassNotFoundException;

    AccountState getOrCreateAccountState(AccountAddress address, Accounts accounts) throws IOException, ClassNotFoundException;

    byte[] convertAccountStateToRLP(AccountState accountState);

    AccountState convertToAccountStateFromRLP(byte[] data);
}
