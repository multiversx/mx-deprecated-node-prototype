package network.elrond.blockchain;

import network.elrond.core.Util;
import network.elrond.data.BlockUtil;
import network.elrond.data.model.Block;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TransactionsPool {

    private static final Logger logger = LogManager.getLogger(TransactionsPool.class);

    private Object locker = new Object();

    protected final Map<String, Object> lastTransactions = new LRUMap<>(100000);
    protected final List<String> transactions = new ArrayList<>();

    public List<String> getTransactions() {
        List<String> newList = null;

        synchronized (locker) {

            newList = new ArrayList<>(transactions);
        }

        return (newList);
    }

    public boolean addTransaction(String transactionHash) {
        Util.check(transactionHash != null, "transaction != null");

        synchronized (locker) {
            if (!checkExistsNoLock(transactionHash)) {
                transactions.add(transactionHash);

                logger.trace("Added {}", transactionHash);

                return (true);
            } else {
                logger.debug("Transaction {} already in pool/processed!", transactionHash);
                return (false);
            }

        }
    }

    public boolean checkExists(String transactionHash) {
        Util.check(transactionHash != null, "transaction != null");

        synchronized (locker) {
            return (checkExistsNoLock(transactionHash));
        }
    }

    protected boolean checkExistsNoLock(String transactionHash) {
        if (transactions.contains(transactionHash)) {
            return true;
        }

        if (lastTransactions.containsKey(transactionHash)) {
            return true;
        }

        return (false);
    }

    public void addBlock(Block block) {
        Util.check(block != null, "block != null");

        if (BlockUtil.isEmptyBlock(block)) {
            return;
        }

        Object dummyObject = new Object();

        synchronized (locker) {
            Collection<String> hashes = BlockUtil.getTransactionsHashesAsString(block);

            for (String hash : hashes) {
                lastTransactions.put(hash, dummyObject);
            }

            transactions.removeAll(hashes);
        }
    }
}
