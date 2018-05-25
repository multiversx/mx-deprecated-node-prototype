package network.elrond.service;

import network.elrond.account.AccountStateService;
import network.elrond.account.AccountStateServiceImpl;
import network.elrond.blockchain.AppPersistenceService;
import network.elrond.blockchain.AppPersistenceServiceImpl;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainServiceImpl;
import network.elrond.consensus.SPoSService;
import network.elrond.consensus.SPoSServiceImpl;
import network.elrond.consensus.ValidatorService;
import network.elrond.consensus.ValidatorServiceImpl;
import network.elrond.crypto.BNMultiSignatureServiceImpl;
import network.elrond.crypto.MultiSignatureService;
import network.elrond.crypto.SchnorrSignatureServiceImpl;
import network.elrond.crypto.SignatureService;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastService;
import network.elrond.p2p.P2PBroadcastServiceImpl;
import network.elrond.p2p.P2PObjectService;
import network.elrond.p2p.P2PObjectServiceImpl;

import java.util.HashMap;
import java.util.Map;

public class AppServiceProvider {

    private static final Map<Class<?>, Object> values = new HashMap<>();

    static {
        InjectDefaultServices();
    }

    public static <T> void putService(Class<T> key, T value) {
        if(value == null){
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
        putService(SignatureService.class, new SchnorrSignatureServiceImpl());
        putService(MultiSignatureService.class, new BNMultiSignatureServiceImpl());
        putService(AccountStateService.class, new AccountStateServiceImpl());
        putService(ExecutionService.class, new ExecutionServiceImpl());
        putService(AppPersistenceService.class, new AppPersistenceServiceImpl());
        putService(BootstrapService.class, new BootstrapServiceImpl());
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

    public static BlockchainService getAppPersistanceService() {
        return (BlockchainService) getService(AppPersistenceService.class);
    }

    public static BootstrapService getBootstrapService() {
        return getService(BootstrapService.class
        );
    }

}
