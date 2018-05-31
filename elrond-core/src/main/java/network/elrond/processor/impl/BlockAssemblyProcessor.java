package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.ThreadUtil;
import network.elrond.data.*;
import network.elrond.p2p.P2PChannelName;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Collect new transactions and put them into new block
 */
public class BlockAssemblyProcessor extends AbstractChannelTask<String> {

    private Logger logger = LoggerFactory.getLogger(BlockAssemblyProcessor.class);

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.TRANSACTION;
    }

    @Override
    protected void process(ArrayBlockingQueue<String> queue, Application application) {


        ThreadUtil.sleep(5000);

        AppContext context = application.getContext();
        if (!context.isSeedNode()) {
            logger.info("Not processing ...");
            return;
        }


        AppState state = application.getState();
        if (state.isLock()) {
            // If sync is running stop
            logger.info("Can't execute, state locked");
            return;
        }

        if (state.getCurrentBlock() == null) {
            // Require bootstrap
            logger.info("Can't execute, bootstrap required");
            return;
        }


        state.setLock();

        proposeBlock(queue, application);

        state.clearLock();

    }

    private void proposeBlock(ArrayBlockingQueue<String> queue, Application application) {


        AppState state = application.getState();

        List<String> hashes = new ArrayList<>(queue);
        queue.clear();

        if (hashes.isEmpty()) {
            logger.info("Can't execute, no transaction");
            return;
        }


        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {

            List<Transaction> transactions = blockchainService.getAll(hashes, blockchain, BlockchainUnitType.TRANSACTION);
            Block block = AppBlockManager.instance().composeBlock(transactions, application);
            AppBlockManager.instance().signBlock(block, application);
            ExecutionService executionService = AppServiceProvider.getExecutionService();
            ExecutionReport result = executionService.processBlock(block, accounts, blockchain);

            if (result.isOk()) {
                //String hash = AppServiceProvider.getSerializationService().getHashString(block);
                //AppServiceProvider.getBlockchainService().put(hash, block, blockchain, BlockchainUnitType.BLOCK);

                String hashBlock = AppServiceProvider.getSerializationService().getHashString(block);
                AppServiceProvider.getBootstrapService().putBlockInBlockchain(block, hashBlock, application.getState());

                logger.info("New block proposed" + hashBlock);
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void process(String hash, Application application) {

    }
}
