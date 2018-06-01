package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.core.ThreadUtil;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.AppBlockManager;
import network.elrond.p2p.P2PChannelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (state.getBlockchain().getCurrentBlock() == null) {
            // Require synchronize
            logger.info("Can't execute, synchronize required");
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
        AppContext context = application.getContext();
        PrivateKey privateKey = context.getPrivateKey();

        AppBlockManager.instance().generateAndBroadcastBlock(hashes, accounts, blockchain, privateKey);

    }



    @Override
    protected void process(String hash, Application application) {

    }
}
