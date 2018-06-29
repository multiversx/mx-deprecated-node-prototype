package network.elrond.blockchain;

import network.elrond.core.CollectionUtil;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.data.BlockUtil;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TransactionsPool {

    public static final int WINDOW_SIZE = 5;

    public Object lock = new Object();

    protected final Map<BigInteger, Collection<String>> map = new ConcurrentHashMap<>();
    protected final ArrayBlockingQueue<String> transactions = new ArrayBlockingQueue<>(50000, true);


    public ArrayBlockingQueue<String> getTransactionPool() {
        return (transactions);
    }

    public void add(String transaction) {
        transactions.add(transaction);
    }


    public boolean checkExists(String transaction) {
        Util.check(transaction != null, "transaction != null");

        for (Collection<String> hashes : map.values()) {
            if (hashes.contains(transaction)) {
                return true;
            }
        }

        return false;
    }

    public void addBlock(Block block) {
        Util.check(block != null, "block != null");

        if (BlockUtil.isEmptyBlock(block)) {
            return;
        }

        List<BigInteger> processedBlockNonces = map.keySet().stream().sorted().collect(Collectors.toList());
        BigInteger currentBlockNonce = block.getNonce();
        if (CollectionUtil.contains(processedBlockNonces, currentBlockNonce)) {
            return;
        }

        Collection<String> hashes = BlockUtil.getTransactionsHashesAsString(block);
        map.put(currentBlockNonce, hashes);

        if (processedBlockNonces.size() + 1 > WINDOW_SIZE) {
            map.remove(processedBlockNonces.get(0));
        }
    }
}
