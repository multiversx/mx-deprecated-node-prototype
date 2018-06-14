package network.elrond.service;

import network.elrond.account.AccountStateService;
import network.elrond.account.AccountStateServiceImpl;
import network.elrond.blockchain.AppPersistenceService;
import network.elrond.blockchain.AppPersistenceServiceImpl;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainServiceImpl;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.ChronologyServiceImpl;
import network.elrond.consensus.SPoSService;
import network.elrond.consensus.SPoSServiceImpl;
import network.elrond.consensus.ValidatorService;
import network.elrond.consensus.ValidatorServiceImpl;
import network.elrond.crypto.*;
import network.elrond.data.*;
import network.elrond.p2p.*;

import java.util.HashMap;
import java.util.Map;

public class AppServiceProvider {

    private static final Map<Class<?>, Object> values = new HashMap<>();

    static {
        InjectDefaultServices();
    }

    public static <T> void putService(Class<T> key, T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        values.put(key, value);
    }

    public static <T> T getService(Class<T> key) {
        return key.cast(values.get(key));
    }

    public static void InjectDefaultServices() {
        putService(P2PBroadcastService.class, new P2PBroadcastServiceImpl());
        putService(SerializationService.class, new SerializationServiceImpl());
        putService(P2PObjectService.class, new P2PObjectServiceImpl());
        putService(TransactionService.class, new TransactionServiceImpl());
        putService(ValidatorService.class, new ValidatorServiceImpl());
        putService(SPoSService.class, new SPoSServiceImpl());
        putService(BlockchainService.class, new BlockchainServiceImpl());
        putService(ECCryptoService.class, new ECCryptoServiceSecp256k1Impl());
        putService(SignatureService.class, new SignatureServiceSchnorrImpl());
        putService(MultiSignatureService.class, new MultiSignatureServiceBNImpl());
        putService(AccountStateService.class, new AccountStateServiceImpl());
        putService(ExecutionService.class, new ExecutionServiceImpl());
        putService(AppPersistenceService.class, new AppPersistenceServiceImpl());
        putService(BootstrapService.class, new BootstrapServiceImpl());
        putService(P2PCommunicationService.class, new P2PCommunicationServiceImpl());
        putService(ChronologyService.class, new ChronologyServiceImpl());
    }

    public static P2PBroadcastService getP2PBroadcastService() {
        return getService(P2PBroadcastService.class);
    }

    public static SerializationService getSerializationService() {
        return getService(SerializationService.class);
    }

    public static P2PObjectService getP2PObjectService() {
        return getService(P2PObjectService.class);
    }

    public static TransactionService getTransactionService() {
        return getService(TransactionService.class);
    }

    public static ValidatorService getValidatorService() {
        return getService(ValidatorService.class);
    }

    public static SPoSService getSPoSService() {
        return getService(SPoSService.class);
    }

    public static BlockchainService getBlockchainService() {
        return getService(BlockchainService.class);
    }

    public static ECCryptoService getECCryptoService() {
        return getService(ECCryptoService.class);
    }

    public static SignatureService getSignatureService() {
        return getService(SignatureService.class);
    }

    public static MultiSignatureService getMultiSignatureService() {
        return getService(MultiSignatureService.class);
    }

    public static AccountStateService getAccountStateService() {
        return getService(AccountStateService.class);
    }

    public static ExecutionService getExecutionService() {
        return getService(ExecutionService.class);
    }

//    public static BlockchainService getAppPersistanceService() {
//        return (BlockchainService) getService(AppPersistenceService.class);
//    }

    public static BootstrapService getBootstrapService() {
        return getService(BootstrapService.class
        );
    }

    public static P2PCommunicationService getP2PCommunicationService(){
        return((P2PCommunicationService)getService(P2PCommunicationService.class));
    }

    public static ChronologyService getChronologyService(){
        return ((ChronologyService)getService(ChronologyService.class));
    }
}
