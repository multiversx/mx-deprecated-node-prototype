package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.p2p.P2PConnection;
import network.elrond.processor.AppProcessor;

import java.io.IOException;

public class BlockchainStarterProcessor implements AppProcessor {

    @Override
    public void process(Application application) throws IOException {

        AppState state = application.getState();


        BlockchainContext context = new BlockchainContext();
        P2PConnection connection = state.getConnection();
        context.setConnection(connection);

        context.setDatabasePath(BlockchainUnitType.BLOCK, "blockchain.block.data");
        context.setDatabasePath(BlockchainUnitType.TRANSACTION, "blockchain.transaction.data");

        Blockchain blockchain = new Blockchain(context);

        state.setBlockchain(blockchain);
    }

}
