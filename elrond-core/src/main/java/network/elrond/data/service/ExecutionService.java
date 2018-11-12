package network.elrond.data.service;

import network.elrond.account.Accounts;
import network.elrond.benchmark.StatisticsManager;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.model.Block;
import network.elrond.data.model.ExecutionReport;
import network.elrond.data.model.Transaction;

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
