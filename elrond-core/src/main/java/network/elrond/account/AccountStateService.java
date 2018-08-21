package network.elrond.account;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.data.Block;
import network.elrond.data.Transaction;
import org.mapdb.Fun;

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

    void initialMintingToKnownAddress(Accounts accounts, int shardId);

    Fun.Tuple2<Block, Transaction> generateGenesisBlock(String initialAddress, BigInteger initialValue, AppState state, AppContext context);
}
