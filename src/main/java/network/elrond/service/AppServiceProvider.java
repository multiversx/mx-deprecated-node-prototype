package network.elrond.service;

import network.elrond.blockchain.AppPersistanceServiceImpl;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainServiceImpl;
import network.elrond.consensus.SPoSService;
import network.elrond.consensus.SPoSServiceImpl;
import network.elrond.consensus.ValidatorService;
import network.elrond.consensus.ValidatorServiceImpl;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastService;
import network.elrond.p2p.P2PBroadcastServiceImpl;
import network.elrond.p2p.P2PObjectService;
import network.elrond.p2p.P2PObjectServiceImpl;

public class AppServiceProvider {

    private static P2PBroadcastService p2PBroadcastService = new P2PBroadcastServiceImpl();
    public static P2PBroadcastService getP2PBroadcastService() {
        return p2PBroadcastService;
    }

    private static SerializationService serializationService = new SerializationServiceImpl();
    public static SerializationService getSerializationService() {
        return serializationService;
    }

    private static P2PObjectService p2PObjectService = new P2PObjectServiceImpl();
    public static P2PObjectService getP2PObjectService() {
        return p2PObjectService;
    }

    private static TransactionService transactionService = new TransactionServiceImpl();
    public static TransactionService getTransactionService() {
        return (transactionService);
    }

    private static BlockService blockService = new BlockServiceImpl();
    public static BlockService getBlockService() {
        return (blockService);
    }

    private static ValidatorService validatorService = new ValidatorServiceImpl();
    public static ValidatorService getValidatorService() {
        return validatorService;
    }

    private static SPoSService sPoSService = new SPoSServiceImpl();
    public static SPoSService getSPoSService() {
        return sPoSService;
    }

    private static BlockchainService blockchainService = new BlockchainServiceImpl();
    public static BlockchainService getBlockchainService() {
        return blockchainService;
    }

    private static BlockchainService appPersistanceService = new AppPersistanceServiceImpl();
    public static BlockchainService getAppPersistanceService() { return appPersistanceService; }

    private static BootstrapService bootstrapService = new BootstrapServiceImpl();
    public static BootstrapService getBootstrapService(){ return bootstrapService;}
}
