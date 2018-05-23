package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.p2p.P2PConnection;
import network.elrond.processor.AppProcessor;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BlockchainStarterProcessor implements AppProcessor {

    public static final String BLOCKCHAIN_BLOCK_DATA = "blockchain.block.data";
    public static final String BLOCKCHAIN_TRANSACTION_DATA = "blockchain.transaction.data";
    public static final String BLOCKCHAIN_SETTINGS_DATA = "blockchain.settings.data";
    public static final String BLOCKCHAIN_BLOCKIDX_DATA = "blockchain.blockidx.data";

    @Override
    public void process(Application application) throws IOException {

        AppContext context = application.getContext();

        AppState state = application.getState();

        String workingDirectory = System.getProperty("user.dir");
        String blockchainBasePath = context.getStorageBasePath();
        Path pathBlk = Paths.get(workingDirectory, blockchainBasePath, BLOCKCHAIN_BLOCK_DATA);
        Path pathTx = Paths.get(workingDirectory, blockchainBasePath, BLOCKCHAIN_TRANSACTION_DATA);
        Path pathSettings = Paths.get(workingDirectory, blockchainBasePath, BLOCKCHAIN_SETTINGS_DATA);
        Path pathBlkIdx = Paths.get(workingDirectory, blockchainBasePath, BLOCKCHAIN_BLOCKIDX_DATA);

        BlockchainContext blockContext = new BlockchainContext();
        P2PConnection connection = state.getConnection();
        blockContext.setConnection(connection);

        blockContext.setDatabasePath(BlockchainUnitType.BLOCK, pathBlk.toString());
        blockContext.setDatabasePath(BlockchainUnitType.TRANSACTION, pathTx.toString());
        blockContext.setDatabasePath(BlockchainUnitType.SETTINGS, pathSettings.toString());
        blockContext.setDatabasePath(BlockchainUnitType.BLOCK_INDEX, pathBlkIdx.toString());

        Blockchain blockchain = new Blockchain(blockContext);

        state.setBlockchain(blockchain);
    }

}
