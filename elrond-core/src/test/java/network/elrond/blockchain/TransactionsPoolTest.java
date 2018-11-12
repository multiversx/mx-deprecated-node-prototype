package network.elrond.blockchain;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.data.BlockUtil;
import network.elrond.data.model.Block;
import network.elrond.data.model.Transaction;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TransactionsPoolTest {
    private static Logger logger = LogManager.getLogger(TransactionsPoolTest.class);

    @Test(expected = IllegalArgumentException.class)
    public void testCheckShouldThrowException() {
        TransactionsPool transactionsProcessed = new TransactionsPool();
        transactionsProcessed.checkExists(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddShouldThrowException() {
        TransactionsPool transactionsProcessed = new TransactionsPool();
        transactionsProcessed.addBlock(null);
    }

    @Test
    public void testAddEmptyBlock() {
        TransactionsPool transactionsProcessed = new TransactionsPool();
        Block emptyBlock = new Block();

        transactionsProcessed.addBlock(emptyBlock);

        TestCase.assertEquals(0, transactionsProcessed.lastTransactions.size());
    }

    @Test
    public void testAddBlock() {
        TransactionsPool transactionsProcessed = new TransactionsPool();
        Block block = new Block();

        for (int i = 0; i < 10; i++) {
            BlockUtil.addTransactionInBlock(block, new byte[]{(byte) i});
        }

        transactionsProcessed.addBlock(block);

        TestCase.assertEquals(10, transactionsProcessed.lastTransactions.size());
        TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 3})));
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 11})));
    }

    @Test
    public void testAdd5BlocksAndCheck() {
        TransactionsPool transactionsProcessed = new TransactionsPool();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(BigInteger.valueOf(5)) < 0; i = i.add(BigInteger.ONE)) {
            Block block = new Block();
            block.setNonce(i);

            for (int j = 0; j < 10; j++) {
                BlockUtil.addTransactionInBlock(block, new byte[]{(byte) (i.longValue() * 10 + j)});
            }

            transactionsProcessed.addBlock(block);
        }

        TestCase.assertEquals(50, transactionsProcessed.lastTransactions.size());
        for (int i = 0; i < 50; i++) {
            TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) i})));
        }
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 50})));
    }

    @Test
    public void testAddMultipleTimes(){
        TransactionsPool transactionsPool = new TransactionsPool();

        TestCase.assertTrue(transactionsPool.addTransaction("aaaa"));
        TestCase.assertFalse(transactionsPool.addTransaction("aaaa"));
    }

    @Test
    public void testAlotOfTransactions(){
        TransactionsPool transactionsPool = new TransactionsPool();

        Block block = new Block();

        List<String> list = new ArrayList<>();

        int max = 100000;
        int percent = 0;
        int oldPercent = 0;

        for (int i = 0; i < max; i++){
            Transaction transaction = new Transaction("a","b", BigInteger.valueOf(i),BigInteger.ZERO, new Shard(0), new Shard(0));
            String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
            byte[] buff = AppServiceProvider.getSerializationService().getHash(transaction);
            block.getListTXHashes().add(buff);

            if (i % 10 == 0){
                transactionsPool.addBlock(block);

                block = new Block();
            }

            if (i % 13 == 0) {
                list.add(hash);
            }

            percent = i * 100 / max;
            if (percent != oldPercent) {
                logger.info("Generating...{}%", percent);
                oldPercent = percent;
            }
        }
        transactionsPool.addBlock(block);

        for (String hash : list){
            TestCase.assertTrue(transactionsPool.checkExists(hash));
        }
    }

    @Test
    public void testAddTransactionAndCheck(){
        TransactionsPool transactionPool = new TransactionsPool();

        transactionPool.addTransaction("aaa");

        TestCase.assertTrue(transactionPool.checkExists("aaa"));
        TestCase.assertFalse(transactionPool.checkExists("bbb"));
    }

}
