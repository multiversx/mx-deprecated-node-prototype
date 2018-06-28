package network.elrond.data;

import network.elrond.account.Accounts;
import network.elrond.benchmark.StatisticsManager;
import network.elrond.blockchain.Blockchain;

public interface ExecutionService {


    /**
     * Process block and update accounts state
     */
    ExecutionReport processBlock(Block block, Accounts accounts, Blockchain blockchain, StatisticsManager statisticsManager);

    /**
     * Process transaction and update accounts state
     */
    ExecutionReport processTransaction(Transaction transaction, Accounts accounts);
}
