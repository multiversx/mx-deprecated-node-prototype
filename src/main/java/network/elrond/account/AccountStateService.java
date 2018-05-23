package network.elrond.account;

import java.io.IOException;

public interface AccountStateService {

    byte[] getHash(AccountState state);

    <A extends String> void rollbackAccountStates(Accounts<A> accounts);

    <A extends String> void commitAccountStates(Accounts<A> accounts);

    <A extends String> void setAccountState(A address, AccountState state, Accounts<A> accounts) throws IOException;

    <A extends String> AccountState getAccountState(A address, Accounts<A> accounts) throws IOException, ClassNotFoundException;

    <A extends String> AccountState getOrCreateAccountState(A address, Accounts<A> accounts) throws IOException, ClassNotFoundException;

}
