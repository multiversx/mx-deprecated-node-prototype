package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.AccountStateService;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.data.BootstrapService;
import network.elrond.data.BootstrapType;
import network.elrond.data.SerializationService;
import network.elrond.data.TransactionService;
import network.elrond.service.AppServiceProvider;

import java.math.BigInteger;

public class BootstrappingProcessorNodeInjector {
    static SerializationService serializationService = AppServiceProvider.getSerializationService();
    static TransactionService transactionService = AppServiceProvider.getTransactionService();
    static BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
    static AccountStateService accountStateService = AppServiceProvider.getAccountStateService();




    public static void main(String[] args) throws Exception{
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4000 /*+ new Random().nextInt(10000)*/);
        context.setNodeName("AAA");
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);
        context.setStorageBasePath("test");

        Application app  = new Application(context);
        AppState state = app.getState();
        app.start();

        //mint region
        //AccountState acsSender = accountStateService.getOrCreateAccountState(AccountAddress.fromPublicKey(pbk1), state.getAccounts());
        //mint 100 ERDs
        //acsSender.setBalance(BigInteger.TEN.pow(10));
        //accountStateService.setAccountState(trx1.getSendAccountAddress(), acsSender, state.getAccounts()); // PMS

    }
}
