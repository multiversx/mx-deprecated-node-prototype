package network.elrond.blockchain;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.data.BlockUtil;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class TransactionsPoolTest {
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

//    @Test
//    public void testAdd7BlocksAndCheck() {
//        TransactionsPool transactionsProcessed = new TransactionsPool();
//
//        for (BigInteger i = BigInteger.ZERO; i.compareTo(BigInteger.valueOf(7)) < 0; i = i.add(BigInteger.ONE)) {
//            Block block = new Block();
//            block.setNonce(i);
//
//            for (int j = 0; j < 10; j++) {
//                BlockUtil.addTransactionInBlock(block, new byte[]{(byte) (i.longValue() * 10 + j)});
//            }
//
//            transactionsProcessed.addBlock(block);
//        }
//
//        TestCase.assertEquals(5, transactionsProcessed.map.size());
//        for (int i = 20; i < 70; i++) {
//            TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) i})));
//        }
//        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 19})));
//        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 70})));
//
//        transactionsProcessed.addTransaction(Util.getDataEncoded64(new byte[]{(byte) 19}));
//        transactionsProcessed.addTransaction(Util.getDataEncoded64(new byte[]{(byte) 20}));
//        transactionsProcessed.addTransaction(Util.getDataEncoded64(new byte[]{(byte) 21}));
//        transactionsProcessed.addTransaction(Util.getDataEncoded64(new byte[]{(byte) 22}));
//
//        byte[] buff = Util.getDataDecoded64("Ew==");
//
//        transactionsProcessed.getTransactions();
//    }

    @Test
    public void testAddTransactionAndCheck(){
        TransactionsPool transactionPool = new TransactionsPool();

        transactionPool.addTransaction("aaa");

        TestCase.assertTrue(transactionPool.checkExists("aaa"));
        TestCase.assertFalse(transactionPool.checkExists("bbb"));
    }

    @Test
    public void testAddTransactionsRemoveListAndCheck(){
        TransactionsPool transactionPool = new TransactionsPool();

        transactionPool.addTransaction("aaa");
        transactionPool.addTransaction("bbb");

        TestCase.assertTrue(transactionPool.checkExists("aaa"));
        TestCase.assertTrue(transactionPool.checkExists("bbb"));

        List<String> transactions = transactionPool.getTransactions();
        transactionPool.removeTransactions(Arrays.asList("aaa", "bbb"));

        TestCase.assertFalse(transactionPool.checkExists("aaa"));
        TestCase.assertFalse(transactionPool.checkExists("bbb"));

        TestCase.assertTrue(transactions.contains("aaa"));
        TestCase.assertTrue(transactions.contains("bbb"));

    }


}
