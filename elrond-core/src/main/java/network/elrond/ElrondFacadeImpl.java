package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

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

        try {

            AppState state = application.getState();
            Accounts accounts = state.getAccounts();

            AccountState account = AppServiceProvider.getAccountStateService().getAccountState(address, accounts);

            return (account == null)? BigInteger.ZERO: account.getBalance();

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    @Override
    public boolean send(AccountAddress receiver, BigInteger value, Application application) {

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
                return false;
            }


            PublicKey receiverPublicKey = receiver.getPublicKey();
            AccountState receiverAccount = AppServiceProvider.getAccountStateService().getOrCreateAccountState(senderAddress, accounts);


            BigInteger nonce = senderAccount.getNonce();
            Transaction transaction = AppServiceProvider.getTransactionService().generateTransaction(senderPublicKey, receiverPublicKey, value, nonce);
            AppServiceProvider.getTransactionService().signTransaction(transaction, senderPrivateKey.getValue());

            String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
            P2PConnection connection = state.getConnection();
            AppServiceProvider.getP2PObjectService().putJsonEncoded(transaction, hash, connection);

            P2PBroadcastChanel channel = state.getChanel(P2PChannelName.TRANSACTION);
            AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, hash);


            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;


    }
}
