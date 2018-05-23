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

    @Override
    public void process(Application application) throws IOException {

        AppContext context = application.getContext();

        AppState state = application.getState();

        final String startupDir = System.getProperty("user.dir");
        Path pathBlk = Paths.get(startupDir, context.getBlockchainBasePath(), "blockchain.block.data");
        Path pathTx = Paths.get(startupDir, context.getBlockchainBasePath(), "blockchain.transaction.data");
        Path pathSettings = Paths.get(startupDir, context.getBlockchainBasePath(), "blockchain.settings.data");
        Path pathBlkIdx = Paths.get(startupDir, context.getBlockchainBasePath(), "blockchain.blockidx.data");

        BlockchainContext blkcContext = new BlockchainContext();
        P2PConnection connection = state.getConnection();
        blkcContext.setConnection(connection);

        blkcContext.setDatabasePath(BlockchainUnitType.BLOCK, pathBlk.toString());
        blkcContext.setDatabasePath(BlockchainUnitType.TRANSACTION, pathTx.toString());
        blkcContext.setDatabasePath(BlockchainUnitType.SETTINGS, pathSettings.toString());
        blkcContext.setDatabasePath(BlockchainUnitType.BLOCK_INDEX, pathBlkIdx.toString());

        Blockchain blockchain = new Blockchain(blkcContext);

        state.setBlockchain(blockchain);
    }

}
