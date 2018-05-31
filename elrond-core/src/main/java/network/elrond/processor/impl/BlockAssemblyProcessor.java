package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Collect new transactions and put them into new block
 */
public class BlockAssemblyProcessor extends AbstractChannelTask<String> {

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.TRANSACTION;
    }

    @Override
    protected void process(ArrayBlockingQueue<String> queue, Application application) {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (!application.getContext().isSeedNode()) {
            //  return;
        }

        AppState state = application.getState();
        if (state.isBootstrapping()) {
            // Bootstrap is running
            return;
        }


        List<String> hashes = new ArrayList<>(queue);
        queue.clear();

        if (hashes.isEmpty()) {
            return;
        }


        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {

            List<Transaction> transactions = blockchainService.getAll(hashes, blockchain, BlockchainUnitType.TRANSACTION);
            Block block = AppBlockManager.instance().composeBlock(transactions, application);
            ExecutionService executionService = AppServiceProvider.getExecutionService();
            ExecutionReport result = executionService.processBlock(block, accounts, blockchain);

            if (result.isOk()) {
                String hash = AppServiceProvider.getSerializationService().getHashString(block);
                AppServiceProvider.getBlockchainService().put(hash, block, blockchain, BlockchainUnitType.BLOCK);

                //TO DO JLS
                //AppServiceProvider.getBootstrapService().setMaxBlockSizeNetwork(block.getNonce(), state.getConnection());
                //AppServiceProvider.getBootstrapService().setMaxBlockSizeLocal(state.getBlockchain(), block.getNonce());
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void process(String hash, Application application) {

    }
}
