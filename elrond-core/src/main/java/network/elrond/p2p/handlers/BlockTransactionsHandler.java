package network.elrond.p2p.handlers;

import javafx.util.Pair;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Block;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PRequestMessage;
import network.elrond.p2p.RequestHandler;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.util.encoders.Base64;

import java.util.ArrayList;

public class BlockTransactionsHandler implements RequestHandler<ArrayList<Pair<String, Transaction>>, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(BlockRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<Pair<String, Transaction>> onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);
        ArrayList<Pair<String, Transaction>> transactionHashPairList = new ArrayList<>();
        data.getKey();
        String blockHash = (String) data.getKey();
        Blockchain blockchain = state.getBlockchain();
        Block block = AppServiceProvider.getBlockchainService().getLocal(blockHash, blockchain, BlockchainUnitType.BLOCK);
        if (block == null) {
            logger.warn("Replying to request: BLOCK_TRANSACTIONS for block hash {} not found", blockHash);
        } else {
            // get list of transactions
            for (byte[] transactionHash : block.getListTXHashes()) {
                String hash = new String(Base64.encode(transactionHash));
                Transaction transaction = AppServiceProvider.getBlockchainService().getLocal(hash, blockchain, BlockchainUnitType.TRANSACTION);
                if (transaction != null) {
                    transactionHashPairList.add(new Pair(hash, transaction));
                } else {
                    logger.warn("Replying to request: BLOCK_TRANSACTIONS {} for block hash {} not found", transactionHash, blockHash);
                }
            }

            logger.warn("Replying to request: BLOCK_TRANSACTIONS with hash {} and {} transactions", blockHash, transactionHashPairList.size());
        }
        return logger.traceExit(transactionHashPairList);
    }
}
