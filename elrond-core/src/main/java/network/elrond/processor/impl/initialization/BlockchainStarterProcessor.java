package network.elrond.processor.impl.initialization;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.p2p.model.P2PConnection;
import network.elrond.processor.AppTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BlockchainStarterProcessor implements AppTask {
    private static final Logger logger = LogManager.getLogger(BlockchainStarterProcessor.class);

    @Override
    public void process(Application application) throws IOException {
        logger.traceEntry("params: {}", application);

        AppContext context = application.getContext();

        AppState state = application.getState();

        String workingDirectory = System.getProperty("user.dir");
        String blockchainBasePath = context.getStorageBasePath();

        BlockchainContext blockContext = new BlockchainContext();
        P2PConnection connection = state.getConnection();
        blockContext.setConnection(connection);

        blockContext.setShard(state.getShard());

        for (BlockchainUnitType type : BlockchainUnitType.values()) {
            Path path = Paths.get(workingDirectory, blockchainBasePath, type.name().toLowerCase());
            blockContext.setDatabasePath(type, path.toString());
        }

        Blockchain blockchain = new Blockchain(blockContext);
        state.setBlockchain(blockchain);

        state.getConnection().getBroadcastHandler().setBlockchain(blockchain);


        logger.traceExit();
    }

}
