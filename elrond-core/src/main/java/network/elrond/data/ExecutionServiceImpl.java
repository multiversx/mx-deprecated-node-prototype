package network.elrond.data;

import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class ExecutionServiceImpl implements ExecutionService {


    private SerializationService serializationService = AppServiceProvider.getSerializationService();

    @Override
    public ExecutionReport processBlock(Block block, Accounts accounts, Blockchain blockchain) {

        try {
            return _processBlock(accounts, blockchain, block);
        } catch (IOException | ClassNotFoundException e) {
            return ExecutionReport.create().ko(e);
        }

    }

    private ExecutionReport _processBlock(Accounts accounts, Blockchain blockchain, Block block) throws IOException, ClassNotFoundException {

        ExecutionReport blockExecutionReport = ExecutionReport.create();

        // Process transactions
        List<Transaction> transactions = AppServiceProvider.getTransactionService().getTransactions(blockchain, block);
        for (Transaction transaction : transactions) {

            ExecutionReport transactionExecutionReport = processTransaction(transaction, accounts);
            if (!transactionExecutionReport.isOk()) {
                blockExecutionReport.combine(transactionExecutionReport);
                break;
            }

        }


        if (blockExecutionReport.isOk()) {
            AppServiceProvider.getAccountStateService().commitAccountStates(accounts);
            blockExecutionReport.ok("Commit account state changes");
        } else {
            AppServiceProvider.getAccountStateService().rollbackAccountStates(accounts);
            blockExecutionReport.ko("Rollback account state changes");
        }


        return blockExecutionReport;
    }


    @Override
    public ExecutionReport processTransaction(Transaction transaction, Accounts accounts) {

        try {
            return _processTransaction(accounts, transaction);
        } catch (Exception e) {
            return ExecutionReport.create().ko(e);
        }
    }

    private ExecutionReport _processTransaction(Accounts accounts, Transaction transaction) throws IOException, ClassNotFoundException {


        if (transaction == null) {
            return ExecutionReport.create().ko("Null transaction");

        }

        String strHash = new String(Base64.encode(serializationService.getHash(transaction)));

        if (!AppServiceProvider.getTransactionService().verifyTransaction(transaction)) {
            return ExecutionReport.create().ko("Invalid transaction! tx hash: " + strHash);
        }

        //We have to copy-construct the objects for sandbox mode
        AccountAddress receiverAddress = transaction.getReceiverAccountAddress();
        AccountState receiverAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(receiverAddress, accounts);

        AccountAddress sendAddress = transaction.getSendAccountAddress();
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(sendAddress, accounts);

        if (senderAccountState.getBalance().compareTo(transaction.getValue()) < 0) {
            return ExecutionReport.create().ko("Invalid transaction! Will result in negative balance! tx hash: " + strHash);
        }

        if (!senderAccountState.getNonce().equals(transaction.getNonce())) {
            return ExecutionReport.create().ko("Invalid transaction! Nonce mismatch! tx hash: " + strHash);
        }

        //transfer asset
        receiverAccountState.setBalance(receiverAccountState.getBalance().add(transaction.getValue()));
        AppServiceProvider.getAccountStateService().setAccountState(receiverAddress, receiverAccountState, accounts); // PMS

        senderAccountState.setBalance(senderAccountState.getBalance().subtract(transaction.getValue()));
        //increase sender nonce
        senderAccountState.setNonce(senderAccountState.getNonce().add(BigInteger.ONE));
        AppServiceProvider.getAccountStateService().setAccountState(sendAddress, senderAccountState, accounts); // PMS

        return ExecutionReport.create().ok();
    }


}
