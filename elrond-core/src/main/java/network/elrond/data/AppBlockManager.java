package network.elrond.data;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AppBlockManager {


    private Logger logger = LoggerFactory.getLogger(AppBlockManager.class);

    private static AppBlockManager instance = new AppBlockManager();

    public static AppBlockManager instance() {
        return instance;
    }


    public Block composeBlock(List<Transaction> transactions, Application application) {

        AppState state = application.getState();
        Block block = new Block();
        Block currentBlock = state.getCurrentBlock();
        byte[] hash = AppServiceProvider.getSerializationService().getHash(currentBlock, true);
        block.setPrevBlockHash(hash);
        for (Transaction transaction : transactions) {
            boolean valid = AppServiceProvider.getTransactionService().verifyTransaction(transaction);
            if (!valid) {
                logger.info("Invalid transaction discarded " + transaction);
                continue;
            }

            byte[] txHash = AppServiceProvider.getSerializationService().getHash(transaction, true);
            block.getListTXHashes().add(txHash);
        }


        return block;
    }
}
