package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppMode;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.ThreadUtil;
import network.elrond.data.*;
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


        ThreadUtil.sleep(5000);


        AppContext context = application.getContext();
        AppState state = application.getState();

        if (!state.isAllowed(AppMode.BLOCK_PROPOSING)) {
            return;
        }

        state.setMode(AppMode.BLOCK_PROPOSING);


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
                //String hash = AppServiceProvider.getSerializationService().getHashString(block);
                //AppServiceProvider.getBlockchainService().put(hash, block, blockchain, BlockchainUnitType.BLOCK);

                String hashBlock = AppServiceProvider.getSerializationService().getHashString(block);
                AppServiceProvider.getBootstrapService().putBlockInBlockchain(block, hashBlock, application.getState());
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        state.setMode(null);
    }

    @Override
    protected void process(String hash, Application application) {

    }
}
