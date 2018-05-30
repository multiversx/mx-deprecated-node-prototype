package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.AccountStateService;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;

public class BootstrappingProcessorNodeProducer {
    static SerializationService serializationService = AppServiceProvider.getSerializationService();
    static TransactionService transactionService = AppServiceProvider.getTransactionService();
    static BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
    static AccountStateService accountStateService = AppServiceProvider.getAccountStateService();

    public static void main(String[] args) throws Exception{
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setNodeName("Producer and main node");
        context.setStorageBasePath("producer");
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);

        //being the main node, the initial public key for minting should be known
        PrivateKey pvKeyInitial = new PrivateKey("INTIAL");
        PublicKey pbKeyInitial = new PublicKey(pvKeyInitial);
        context.setStrAddressMint(Util.byteArrayToHexString(pbKeyInitial.getValue()));

        //1 mil ERD's go to initial address
        context.setValueMint(BigInteger.TEN.pow(14));

        //================================Setup done

        Application app  = new Application(context);
        AppState state = app.getState();
        app.start();

        while (true){

            Thread.sleep(1000);

            System.out.println("Local height: " + bootstrapService.getMaxBlockSize(LocationType.LOCAL, state.getBlockchain()).toString(10) +
                            ", network height: " + bootstrapService.getMaxBlockSize(LocationType.NETWORK, state.getBlockchain()).toString(10) +
                    ", app state hash: " + new String(Base64.encode(state.getAccounts().getAccountsPersistenceUnit().getRootHash())));
        }

    }
}
