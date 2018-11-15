package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.model.Block;
import network.elrond.data.model.Transaction;
import network.elrond.p2p.RequestHandler;
import network.elrond.p2p.model.P2PRequestMessage;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.util.encoders.Base64;

import java.util.ArrayList;

public class BlockTransactionsHandler implements RequestHandler<ArrayList<Transaction>, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(BlockRequestHandler.class);

    @Override
    public ArrayList<Transaction> onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);
        ArrayList<Transaction> transactionList = new ArrayList<>();
        data.getKey();
        String blockHash = (String) data.getKey();
        Blockchain blockchain = state.getBlockchain();
        Block block = AppServiceProvider.getBlockchainService().getLocal(blockHash, blockchain, BlockchainUnitType.BLOCK);
        if (block == null) {
            logger.warn("Replying to request: BLOCK_TRANSACTIONS for block hash {} not found", blockHash);
        } else {
            // get list of transactions
            for (byte[] transactionHash : block.getListTXHashes()) {
                Transaction transaction = AppServiceProvider.getBlockchainService().getLocal(new String(Base64.encode(transactionHash)), blockchain, BlockchainUnitType.TRANSACTION);
                if (transaction != null) {
                    transactionList.add(transaction);
                } else {
                    logger.warn("Replying to request: BLOCK_TRANSACTIONS {} for block hash {} not found", transactionHash, blockHash);
                }
            }

            logger.warn("Replying to request: BLOCK_TRANSACTIONS with hash {} and {} transactions", blockHash, transactionList.size());
        }
        return logger.traceExit(transactionList);
    }
}
