package network.elrond.service;

import network.elrond.account.AccountStateService;
import network.elrond.account.AccountStateServiceImpl;
import network.elrond.blockchain.AppPersistenceService;
import network.elrond.blockchain.AppPersistenceServiceImpl;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainServiceImpl;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.ChronologyServiceImpl;
import network.elrond.consensus.*;
import network.elrond.crypto.*;
import network.elrond.data.*;
import network.elrond.data.service.BootstrapService;
import network.elrond.data.service.BootstrapServiceImpl;
import network.elrond.data.service.ExecutionService;
import network.elrond.data.service.ExecutionServiceImpl;
import network.elrond.data.service.SerializationService;
import network.elrond.data.service.SerializationServiceImpl;
import network.elrond.data.service.TransactionService;
import network.elrond.data.service.TransactionServiceImpl;
import network.elrond.p2p.*;
import network.elrond.p2p.service.P2PBroadcastService;
import network.elrond.p2p.service.P2PBroadcastServiceImpl;
import network.elrond.p2p.service.P2PCommunicationService;
import network.elrond.p2p.service.P2PCommunicationServiceImpl;
import network.elrond.p2p.service.P2PConnectionService;
import network.elrond.p2p.service.P2PConnectionServiceImpl;
import network.elrond.p2p.service.P2PRequestService;
import network.elrond.p2p.service.P2PRequestServiceImpl;
import network.elrond.sharding.ShardingService;
import network.elrond.sharding.ShardingServiceImpl;

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
        putService(ShardingService.class, new ShardingServiceImpl());
        putService(P2PConnectionService.class, new P2PConnectionServiceImpl());
        putService(P2PRequestService.class, new P2PRequestServiceImpl());
        putService(ConsensusService.class, new ConsensusServiceImpl());
    }

    public static P2PBroadcastService getP2PBroadcastService() {
        return getService(P2PBroadcastService.class);
    }

    public static SerializationService getSerializationService() {
        return getService(SerializationService.class);
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

    public static ShardingService getShardingService() {
        return getService(ShardingService.class);
    }

    public static P2PConnectionService getP2PConnectionService() {
        return getService(P2PConnectionService.class);
    }

    public static P2PRequestService getP2PRequestService() {
        return getService(P2PRequestService.class);
    }

    public static BootstrapService getBootstrapService() {
        return getService(BootstrapService.class);
    }

    public static P2PCommunicationService getP2PCommunicationService() {
        return getService(P2PCommunicationService.class);
    }

    public static ChronologyService getChronologyService() {
        return getService(ChronologyService.class);
    }

    public static ConsensusService getConsensusService() {return getService(ConsensusService.class);}
}
