package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.AccountStateService;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.BootstrapService;
import network.elrond.data.LocationType;
import network.elrond.data.SerializationService;
import network.elrond.data.TransactionService;
import network.elrond.service.AppServiceProvider;
import org.spongycastle.util.encoders.Base64;

public class BootstrappingProcessorNodeConsumer {

    static SerializationService serializationService = AppServiceProvider.getSerializationService();
    static TransactionService transactionService = AppServiceProvider.getTransactionService();
    static BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
    static AccountStateService accountStateService = AppServiceProvider.getAccountStateService();

    public static void main(String[] args) throws Exception{

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001 /*+ new Random().nextInt(10000)*/);
        context.setNodeName("consumer");
        context.setStorageBasePath("consumer");

        PrivateKey pvKeyInitial = new PrivateKey("CONSUMER");
        context.setPrivateKey(pvKeyInitial);

        Application app = new Application(context);
        app.start();

        AppState state = app.getState();

        while (true){

            Thread.sleep(1000);

            System.out.println("Local height: " + bootstrapService.getCurrentBlockIndex(LocationType.LOCAL, state.getBlockchain()).toString(10) +
                    ", network height: " + bootstrapService.getCurrentBlockIndex(LocationType.NETWORK, state.getBlockchain()).toString(10) +
                    ", app state hash: " + new String(Base64.encode(state.getAccounts().getAccountsPersistenceUnit().getRootHash())));
        }


    }


}
