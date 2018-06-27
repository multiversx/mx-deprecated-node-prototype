package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.AccountStateService;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.service.AppServiceProvider;
import org.spongycastle.util.encoders.Base64;

import java.math.BigInteger;

public class BootstrappingProcessorNodeProducer {
    static SerializationService serializationService = AppServiceProvider.getSerializationService();
    static TransactionService transactionService = AppServiceProvider.getTransactionService();
    static BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
    static AccountStateService accountStateService = AppServiceProvider.getAccountStateService();

    public static void main(String[] args) throws Exception{
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress(null);
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setNodeName("Producer and main node");
        context.setStorageBasePath("producer");
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);


        //being the main node, the initial public key for minting should be known
        PrivateKey pvKeyInitial = new PrivateKey("INITIAL");
        PublicKey pbKeyInitial = new PublicKey(pvKeyInitial);
        context.setStrAddressMint(Util.byteArrayToHexString(pbKeyInitial.getValue()));
        context.setPrivateKey(pvKeyInitial);

        //1 mil ERD's go to initial address
        //context.setValueMint(BigInteger.TEN.pow(14));

        //================================Setup done

        Application app  = new Application(context);
        AppState state = app.getState();
        app.start();

        PrivateKey pvKeyRecv = new PrivateKey("CONSUMER");
        PublicKey pbKeyRecv = new PublicKey(pvKeyRecv);

        while (true){

            printAccountsWithBalance(state.getAccounts());

            Thread.sleep(1000);

            //generate one transaction to execute
            //step 1. get nonce for sender account
            AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromBytes(pbKeyInitial.getValue()), state.getAccounts());

            if (accountState == null){
                System.out.println("NULL account?");
            } else {
                BigInteger nonceTransaction = accountState.getNonce();
                BigInteger balance = accountState.getBalance();

                if (balance.compareTo(BigInteger.ZERO) == 0){
                    System.out.println("Run out of sERD's?");
                } else {
                    Transaction tx = AppServiceProvider.getTransactionService().generateTransaction(pbKeyInitial, pbKeyRecv, BigInteger.ONE, nonceTransaction);
                    AppServiceProvider.getTransactionService().signTransaction(tx, pvKeyInitial.getValue(), pbKeyInitial.getValue());

                    P2PBroadcastChanel channel = state.getChanel(P2PBroadcastChannelName.TRANSACTION);

                    System.out.println("Put tx hash: " + AppServiceProvider.getSerializationService().getHashString(tx));
                    System.out.println(AppServiceProvider.getSerializationService().encodeJSON(tx));

                    AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, tx);
                }
            }
            System.out.println("Local height: " + bootstrapService.getCurrentBlockIndex(LocationType.LOCAL, state.getBlockchain()).toString(10) +
                            ", network height: " + bootstrapService.getCurrentBlockIndex(LocationType.NETWORK, state.getBlockchain()).toString(10) +
                    ", app state hash: " + new String(Base64.encode(state.getAccounts().getAccountsPersistenceUnit().getRootHash())));
        }

    }

    public static void printAccountsWithBalance(Accounts accounts){
        System.out.println("Accounts: ");
        System.out.println("================================================================");

        if (accounts == null){
            System.out.println(" * NULL accounts object!");
            System.out.println("================================================================");
            return;
        }

        if (accounts.getAddresses().size() == 0){
            System.out.println(" * EMPTY set!");
            System.out.println("================================================================");
            return;
        }

        AccountState accountState;

        for (AccountAddress accountAddress : accounts.getAddresses()){

            try {
                accountState = AppServiceProvider.getAccountStateService().getAccountState(accountAddress, accounts);
            } catch(Exception ex) {
                ex.printStackTrace();
                continue;
            }

            System.out.println(Util.byteArrayToHexString(accountAddress.getBytes()) + ": nonce " +
                    accountState.getNonce().toString(10) + "; balance " +
                    accountState.getBalance().toString(10));
        }



//        for ( entry: nodes.keySet()){
//
//        }
        System.out.println("================================================================");

    }
}
