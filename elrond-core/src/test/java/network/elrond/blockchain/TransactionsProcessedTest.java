package network.elrond.blockchain;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.data.BlockUtil;
import org.junit.Test;

import java.math.BigInteger;

public class TransactionsProcessedTest {
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

        TestCase.assertEquals(0, transactionsProcessed.map.size());
    }

    @Test
    public void testAddBlock() {
        TransactionsPool transactionsProcessed = new TransactionsPool();
        Block block = new Block();

        for (int i = 0; i < 10; i++) {
            BlockUtil.addTransactionInBlock(block, new byte[]{(byte) i});
        }

        transactionsProcessed.addBlock(block);

        TestCase.assertEquals(1, transactionsProcessed.map.size());
        TestCase.assertEquals(10, transactionsProcessed.map.get(BigInteger.ZERO).size());
        TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 3})));
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 11})));
    }

    @Test
    public void testAdd5BlocksAnCheck() {
        TransactionsPool transactionsProcessed = new TransactionsPool();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(BigInteger.valueOf(5)) < 0; i = i.add(BigInteger.ONE)) {
            Block block = new Block();
            block.setNonce(i);

            for (int j = 0; j < 10; j++) {
                BlockUtil.addTransactionInBlock(block, new byte[]{(byte) (i.longValue() * 10 + j)});
            }

            transactionsProcessed.addBlock(block);
        }

        TestCase.assertEquals(5, transactionsProcessed.map.size());
        for (int i = 0; i < 50; i++) {
            TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) i})));
        }
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 50})));
    }

    @Test
    public void testAdd7BlocksAnCheck() {
        TransactionsPool transactionsProcessed = new TransactionsPool();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(BigInteger.valueOf(7)) < 0; i = i.add(BigInteger.ONE)) {
            Block block = new Block();
            block.setNonce(i);

            for (int j = 0; j < 10; j++) {
                BlockUtil.addTransactionInBlock(block, new byte[]{(byte) (i.longValue() * 10 + j)});
            }

            transactionsProcessed.addBlock(block);
        }

        TestCase.assertEquals(5, transactionsProcessed.map.size());
        for (int i = 20; i < 70; i++) {
            TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) i})));
        }
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 19})));
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte) 70})));
    }


}
