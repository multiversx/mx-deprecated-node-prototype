package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Block;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.AppProcessors;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class P2PBlocksInterceptorProcessor extends AbstractChannelProcessor<String> {

    private Logger logger = LoggerFactory.getLogger(AppProcessors.class);

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.BLOCK;
    }

    @Override
    protected void process(String hash, Application application) {

        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        Accounts accounts = state.getAccounts();

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {

            // This will retrieve block from network if required
            Block block = blockchainService.get(hash, blockchain, BlockchainUnitType.BLOCK);
            if (block != null) {

                blockchainService.put(block.getNonce(), hash, blockchain, BlockchainUnitType.BLOCK_INDEX);
                AppServiceProvider.getExecutionService().processBlock(block, accounts, blockchain);

                logger.info("Got new block " + hash);
            } else {
                logger.info("Block not found !!!: " + hash);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


}
