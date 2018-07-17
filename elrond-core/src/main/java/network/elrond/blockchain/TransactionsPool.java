package network.elrond.blockchain;

import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.data.BlockUtil;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TransactionsPool {

    //public static final int WINDOW_SIZE = 5;

    private static final Logger logger = LogManager.getLogger(TransactionsPool.class);

    private Object locker = new Object();

    protected final Map<String, Object> lastTransactions = new LRUMap<>(100000);
    protected final List<String> transactions = new ArrayList<>();

    public List<String> getTransactions(){
        List<String> newList = null;

        synchronized (locker){
            //cleanTransactionList();

            newList = new ArrayList<>(transactions);
        }

        logger.debug("Returned the transactions: {}", newList);

        return (newList);
    }

//    public void removeTransactions(List<String> hashes){
//        Util.check(hashes != null, "hashes != null");
//
//        synchronized (locker){
//            logger.debug("About to remove {} transactions from a total of {} transactions", hashes.size(), transactions.size());
//
//            transactions.removeAll(hashes);
//
//            logger.debug("Remained {} transactions", transactions.size());
//        }
//    }

    public boolean addTransaction(String transactionHash){
        Util.check(transactionHash != null, "transaction != null");

        synchronized (locker){
            if (!checkExistsNoLock(transactionHash)) {
                transactions.add(transactionHash);

                logger.debug("Added {}", transactionHash);

                return(true);
            } else {
                logger.debug("Transaction {} already in pool/processed!", transactionHash);
                return(false);
            }

        }
    }

    public boolean checkExists(String transactionHash){
        Util.check(transactionHash != null, "transaction != null");

        synchronized (locker){
            return(checkExistsNoLock(transactionHash));
        }
    }

    protected boolean checkExistsNoLock(String transactionHash){
        if (transactions.contains(transactionHash)){
            return true;
        }

        if (lastTransactions.containsKey(transactionHash)){
            return true;
        }

        return(false);
    }

//    private void cleanTransactionList() {
//        logger.debug("About to clean transaction pool. There are {} transactions", transactions.size());
//
//        transactions.removeAll(lastTransactions.keySet());
//
//        logger.debug("Remained {} transactions", transactions.size());
//    }

    //public ArrayBlockingQueue<String> getTransactionPool() {
    //    return (transactions);
    //}

    //public void add(String transaction) {
    //    transactions.add(transaction);
    //}

    public void addBlock(Block block) {
        Util.check(block != null, "block != null");

        if (BlockUtil.isEmptyBlock(block)) {
            return;
        }

        Object dummyObject = new Object();

        synchronized (locker) {
            Collection<String> hashes = BlockUtil.getTransactionsHashesAsString(block);

            for (String hash : hashes){
                lastTransactions.put(hash, dummyObject);
            }

            transactions.removeAll(hashes);


//            List<BigInteger> processedBlockNonces = map.keySet().stream().sorted().collect(Collectors.toList());
//            BigInteger currentBlockNonce = block.getNonce();
//            if (CollectionUtil.contains(processedBlockNonces, currentBlockNonce)) {
//                return;
//            }
//
//
//            map.put(currentBlockNonce, hashes);
//
//            if (processedBlockNonces.size() + 1 > WINDOW_SIZE) {
//                map.remove(processedBlockNonces.get(0));
//                logger.debug("Removed from transaction pool block with nonce {}", processedBlockNonces.get(0));
//            }

            //cleanTransactionList();
        }
    }
}
