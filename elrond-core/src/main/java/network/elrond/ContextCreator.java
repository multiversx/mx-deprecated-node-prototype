package network.elrond;

import network.elrond.application.AppContext;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.BootstrapType;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import network.elrond.sharding.ShardingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ContextCreator {
    private static final Logger logger = LogManager.getLogger(ContextCreator.class);

    public static AppContext createAppContext(String nodeName, String nodePrivateKeyString, String masterPeerIpAddress,
                                              Integer masterPeerPort, Integer port, BootstrapType bootstrapType,
                                              String blockchainPath, boolean isSeeder) throws Exception {
        logger.traceEntry("params: {} {} {} {} {} {} {}", nodeName, nodePrivateKeyString, masterPeerIpAddress,
                masterPeerPort, port, bootstrapType, blockchainPath);

        if(bootstrapType == BootstrapType.START_FROM_SCRATCH){
            Util.deleteDirectory(new File(blockchainPath));
        }

        AppContext context = new AppContext();

        context.setMasterPeerIpAddress(masterPeerIpAddress);
        context.setMasterPeerPort(masterPeerPort);
        context.setPort(port);
        context.setNodeName(nodeName);
        context.setStorageBasePath(blockchainPath);
        context.setSeeder(isSeeder);

        context.setBootstrapType(bootstrapType);
        PrivateKey nodePrivateKey = new PrivateKey(Util.hexStringToByteArray(nodePrivateKeyString));
        context.setPrivateKey(nodePrivateKey);


        PublicKey mintPublicKey = new PublicKey(nodePrivateKey);
        String mintAddress = Util.getAddressFromPublicKey(mintPublicKey.getValue());
        context.setStrAddressMint(mintAddress);

        ShardingService shardingService = AppServiceProvider.getShardingService();
        PublicKey preGeneratedPublicKey = new PublicKey(context.getPrivateKey());
        Shard shard = shardingService.getShard(preGeneratedPublicKey.getValue());

        if ((isSeeder) && (shard.getIndex() != 0)){
            throw new Exception("Seeder must start in shard 0, not shard " + String.valueOf(shard.getIndex()) + "!");
        }

        return logger.traceExit(context);
    }

}
