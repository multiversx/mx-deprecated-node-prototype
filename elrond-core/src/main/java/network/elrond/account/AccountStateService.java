package network.elrond.account;

import network.elrond.data.GenesisBlock;

import java.io.IOException;
import java.math.BigInteger;

public interface AccountStateService {

    void rollbackAccountStates(Accounts accounts);

    void commitAccountStates(Accounts accounts);

    void setAccountState(AccountAddress address, AccountState state, Accounts accounts) throws IOException;

    AccountState getAccountState(AccountAddress address, Accounts accounts) throws IOException, ClassNotFoundException;

    AccountState getOrCreateAccountState(AccountAddress address, Accounts accounts) throws IOException, ClassNotFoundException;

    byte[] convertAccountStateToRLP(AccountState accountState);

    AccountState convertToAccountStateFromRLP(byte[] data);

    void initialMintingToKnownAddress(Accounts accounts);

    GenesisBlock generateGenesisBlock(String strAddressMint, BigInteger startValue, AccountsContext accountsContextTemporary);
}
