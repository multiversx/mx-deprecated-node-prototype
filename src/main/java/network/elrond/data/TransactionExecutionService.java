package network.elrond.data;

import network.elrond.account.Accounts;

public interface TransactionExecutionService {

    <A extends String> boolean processTransaction(Accounts<A> accounts, Transaction transaction) throws Exception;
}
