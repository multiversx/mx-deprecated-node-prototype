package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PKSKPair;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.p2p.P2PConnection;
import network.elrond.p2p.PingResponse;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ElrondFacadeImpl implements ElrondFacade {

    private static final Logger logger = LoggerFactory.getLogger("ElrondFacadeImpl");

    @Override
    public Application start(AppContext context) {

        try {
            Application application = new Application(context);
            application.start();
            return application;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean stop(Application application) {

        try {
            application.stop();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BigInteger getBalance(AccountAddress address, Application application) {

        if (application == null) {
            return BigInteger.ZERO;
        }

        try {

            AppState state = application.getState();
            Accounts accounts = state.getAccounts();

            AccountState account = AppServiceProvider.getAccountStateService().getAccountState(address, accounts);

            return (account == null) ? BigInteger.ZERO : account.getBalance();

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Receipt getReceipt(String transactionHash, Application application) {

        try {
            FutureTask<Receipt> timeoutTask = new FutureTask<Receipt>(() -> {
                Blockchain blockchain = application.getState().getBlockchain();
                String receiptHash;
                do {
                    receiptHash = AppServiceProvider.getBlockchainService().get(transactionHash, blockchain, BlockchainUnitType.TRANSACTION_RECEIPT);
                    ThreadUtil.sleep(200);
                } while (receiptHash == null);
                return AppServiceProvider.getBlockchainService().get(receiptHash, blockchain, BlockchainUnitType.RECEIPT);
            });
            new Thread(timeoutTask).start();
            return timeoutTask.get(30L, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Transaction send(AccountAddress receiver, BigInteger value, Application application) {

        if (application == null) {
            logger.info("Invalid application state, application is null");
            return null;
        }


        try {

            AppState state = application.getState();
            Accounts accounts = state.getAccounts();


            PublicKey senderPublicKey = state.getPublicKey();
            PrivateKey senderPrivateKey = state.getPrivateKey();
            AccountAddress senderAddress = AccountAddress.fromPublicKey(senderPublicKey);
            AccountState senderAccount = AppServiceProvider.getAccountStateService().getAccountState(senderAddress, accounts);


            if (senderAccount == null) {
                // sender account is new, can't send
                logger.info("Sender account is new, can't send");
                return null;
            }


            PublicKey receiverPublicKey = receiver.getPublicKey();

            BigInteger nonce = senderAccount.getNonce();
            Transaction transaction = AppServiceProvider.getTransactionService().generateTransaction(senderPublicKey, receiverPublicKey, value, nonce);
            AppServiceProvider.getTransactionService().signTransaction(transaction, senderPrivateKey.getValue(), senderPublicKey.getValue());

            String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
            P2PConnection connection = state.getConnection();
            AppServiceProvider.getP2PObjectService().put(connection, hash, transaction);

            P2PBroadcastChanel channel = state.getChanel(P2PChannelName.TRANSACTION);
            AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, hash);


            return transaction;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public PingResponse ping(String ipAddress, int port) {
        try {
            return (AppServiceProvider.getP2PCommunicationService().getPingResponse(ipAddress, port));
        } catch (Exception ex) {
            ex.printStackTrace();
//            return (new PingResponse());
            PingResponse pingResponse = new PingResponse();
            pingResponse.setErrorMessage(ex.getLocalizedMessage());
            return (pingResponse);
        }
    }

    @Override
    public PKSKPair generatePublicKeyAndPrivateKey() {
        try {
            PrivateKey privateKey = new PrivateKey();
            PublicKey publicKey = new PublicKey(privateKey);
            return new PKSKPair(Util.byteArrayToHexString(publicKey.getValue()), Util.byteArrayToHexString(privateKey.getValue()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new PKSKPair("Error", "Error");
        }
    }

    @Override
    public PKSKPair generatePublicKeyFromPrivateKey(String strPrivateKey) {
        try {
            PrivateKey privateKey = new PrivateKey(Util.hexStringToByteArray(strPrivateKey));
            PublicKey publicKey = new PublicKey(privateKey);
            return new PKSKPair(Util.byteArrayToHexString(publicKey.getValue()), Util.byteArrayToHexString(privateKey.getValue()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new PKSKPair("Error", "Error");
        }
    }


}
