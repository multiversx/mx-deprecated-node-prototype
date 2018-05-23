package network.elrond.data;

import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class TransactionExecutionServiceImpl implements TransactionExecutionService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public <A extends String> boolean processTransaction(Accounts<A> accounts, Transaction transaction) throws Exception {

        if (transaction == null) {
            return false;
        }


        String strHash = new String(Base64.encode(AppServiceProvider.getTransactionService().getHash(transaction, true)));

        if (!AppServiceProvider.getTransactionService().verifyTransaction(transaction)) {
            logger.info("Invalid transaction! tx hash: " + strHash);
            return false;
        }

        //We have to copy-construct the objects for sandbox mode
        A receiverAddress = (A) transaction.getReceiverAddress();
        AccountState receiverAccountState = AppServiceProvider.getAccountStateService().getAccountState(receiverAddress, accounts);

        A sendAddress = (A) transaction.getSendAddress();
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getAccountState(sendAddress, accounts);

        if (senderAccountState.getBalance().compareTo(transaction.getValue()) < 0) {
            logger.info("Invalid transaction! Will result in negative balance! tx hash: " + strHash);
            return false;
        }

        if (!senderAccountState.getNonce().equals(transaction.getNonce())) {
            logger.info("Invalid transaction! Nonce mismatch! tx hash: " + strHash);
            return false;
        }

        //transfer asset
        receiverAccountState.setBalance(receiverAccountState.getBalance().add(transaction.getValue()));
        senderAccountState.setBalance(senderAccountState.getBalance().subtract(transaction.getValue()));
        //increase sender nonce
        senderAccountState.setNonce(senderAccountState.getNonce().add(BigInteger.ONE));

        return true;


    }


}
