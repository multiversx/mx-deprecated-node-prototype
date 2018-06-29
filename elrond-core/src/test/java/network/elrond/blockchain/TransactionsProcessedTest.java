package network.elrond.blockchain;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.data.Block;
import org.junit.Test;

import java.math.BigInteger;

public class TransactionsProcessedTest {
    @Test (expected = IllegalArgumentException.class)
    public void testCheckShouldThrowException(){
        TransactionsProcessed transactionsProcessed = new TransactionsProcessed();
        transactionsProcessed.checkExists(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddShouldThrowException(){
        TransactionsProcessed transactionsProcessed = new TransactionsProcessed();
        transactionsProcessed.addBlock(null);
    }

    @Test
    public void testAddEmptyBlock(){
        TransactionsProcessed transactionsProcessed = new TransactionsProcessed();
        Block emptyBlock = new Block();

        transactionsProcessed.addBlock(emptyBlock);

        TestCase.assertEquals(0, transactionsProcessed.transactionsProcessed.size());
    }

    @Test
    public void testAddBlock(){
        TransactionsProcessed transactionsProcessed = new TransactionsProcessed();
        Block block = new Block();

        for (int i = 0; i< 10; i++){
            block.getListTXHashes().add(new byte[]{(byte)i});
        }

        transactionsProcessed.addBlock(block);

        TestCase.assertEquals(1, transactionsProcessed.transactionsProcessed.size());
        TestCase.assertEquals(10, transactionsProcessed.transactionsProcessed.get(BigInteger.ZERO).size());
        TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte)3})));
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte)11})));
    }

    @Test
    public void testAdd5BlocksAnCheck(){
        TransactionsProcessed transactionsProcessed = new TransactionsProcessed();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(BigInteger.valueOf(5)) < 0; i = i.add(BigInteger.ONE)) {
            Block block = new Block();
            block.setNonce(i);

            for (int j = 0; j < 10; j++) {
                block.getListTXHashes().add(new byte[]{(byte)(i.longValue()*10 + j)});
            }

            transactionsProcessed.addBlock(block);
        }

        TestCase.assertEquals(5, transactionsProcessed.transactionsProcessed.size());
        for (int i = 0; i < 50; i++) {
            TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte)i})));
        }
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte)50})));
    }

    @Test
    public void testAdd7BlocksAnCheck(){
        TransactionsProcessed transactionsProcessed = new TransactionsProcessed();

        for (BigInteger i = BigInteger.ZERO; i.compareTo(BigInteger.valueOf(7)) < 0; i = i.add(BigInteger.ONE)) {
            Block block = new Block();
            block.setNonce(i);

            for (int j = 0; j < 10; j++) {
                block.getListTXHashes().add(new byte[]{(byte)(i.longValue()*10 + j)});
            }

            transactionsProcessed.addBlock(block);
        }

        TestCase.assertEquals(5, transactionsProcessed.transactionsProcessed.size());
        for (int i = 20; i < 70; i++) {
            TestCase.assertTrue(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte)i})));
        }
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte)19})));
        TestCase.assertFalse(transactionsProcessed.checkExists(Util.getDataEncoded64(new byte[]{(byte)70})));
    }


}
