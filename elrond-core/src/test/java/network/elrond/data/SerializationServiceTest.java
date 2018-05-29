package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SerializationServiceTest {
    private PrivateKey pvKeySender = new PrivateKey("a");
    private PublicKey pbKeySender = new PublicKey(pvKeySender);

    private PrivateKey pvKeyRecv = new PrivateKey("b");
    private PublicKey pbKeyRecv = new PublicKey(pvKeyRecv);

    SerializationService serializationService = null;
    TransactionService transactionService = null;

    @Before
    public void SetUp(){
        AppServiceProvider.InjectDefaultServices();
        serializationService = AppServiceProvider.getSerializationService();
        transactionService = AppServiceProvider.getTransactionService();
    }

    @After
    public void TearDown(){
    }

    @Test
    public void testEncodeDecodeWithFilterAndNullFilterValueShouldNotThrowException(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);
        String encodedTransaction = serializationService.encodeJSON(tx, null, tx.getIgnoredFields());

        for (String ignoredField : tx.getIgnoredFields()) {
            TestCase.assertTrue(encodedTransaction.contains(ignoredField));
        }

    }

    @Test
    public void testEncodeDecodeWithFilterAndEmptyFilterValueShouldNotThrowException(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);
        String encodedTransaction = serializationService.encodeJSON(tx, "", tx.getIgnoredFields());

        for (String ignoredField : tx.getIgnoredFields()) {
            TestCase.assertTrue(encodedTransaction.contains(ignoredField));
        }
    }

    @Test
    public void testEncodeDecodeWithFilterAndNullIgnoredFieldsShouldNotThrowException(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);
        String encodedTransaction = serializationService.encodeJSON(tx, Util.SIGNATURE_FILTER, null);

        for (String ignoredField : tx.getIgnoredFields()) {
            TestCase.assertTrue(encodedTransaction.contains(ignoredField));
        }

    }

    @Test
    public void testEncodeDecodeWithFilterAndEmptyIgnoredFieldsShouldNotThrowException(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);
        String encodedTransaction = serializationService.encodeJSON(tx, Util.SIGNATURE_FILTER, new String[0]);

        for (String ignoredField : tx.getIgnoredFields()) {
            TestCase.assertTrue(encodedTransaction.contains(ignoredField));
        }
    }

    @Test
    public void testTransactionEncodeDecodeWithoutFilter(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);
        String encodedTransaction = serializationService.encodeJSON(tx);
        //System.out.println(strEncoded);
        Transaction decodedTransaction = serializationService.decodeJSON(encodedTransaction, Transaction.class);

        TestCase.assertEquals(serializationService.encodeJSON(tx), serializationService.encodeJSON(decodedTransaction));
    }

    @Test
    public void testTransactionEncodeDecodeWithFilter(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);
        String encodedTransaction = serializationService.encodeJSON(tx, Util.SIGNATURE_FILTER, tx.getIgnoredFields());
        //System.out.println(strEncoded);
        Transaction decodedTransaction = serializationService.decodeJSON(encodedTransaction, Transaction.class);

        TestCase.assertEquals(serializationService.encodeJSON(tx), serializationService.encodeJSON(decodedTransaction));
    }

    @Test
    public void testBlkEncodeDecode() {
        Block blk = getTestBlock();

        String encodedBlock = serializationService.encodeJSON(blk);
        String encodedBlockWithFilter = serializationService.encodeJSON(blk, Util.SIGNATURE_FILTER, blk.getIgnoredFields());

        //System.out.println(strEncoded);
        //System.out.println(strEncoded2);

        Block decodedBlock = serializationService.decodeJSON(encodedBlock, DataBlock.class);

        TestCase.assertEquals(encodedBlock, serializationService.encodeJSON(decodedBlock));
        TestCase.assertFalse(encodedBlockWithFilter.contains("sig1"));
    }



    @Test
    public void testBlockHashWithSig() {
        Block blk = getTestBlock();

        byte[] hash = serializationService.getHash(blk, true);
    }

    @Test
    public void testBlockHashStringWithSig() {
        Block blk = getTestBlock();

        String hashString = new String(Base64.encode(serializationService.getHash(blk, true)));
        String blkHashString = serializationService.getHashString(blk, true);

        Assert.assertEquals(hashString, blkHashString);
    }

    @Test
    public void testBlockHashStringWithoutSig() {
        Block blk = getTestBlock();

        String hashString = new String(Base64.encode(serializationService.getHash(blk, false)));
        String blkHashString = serializationService.getHashString(blk, false);

        Assert.assertEquals(hashString, blkHashString);
    }

    private Block getTestBlock() {
        Block blk = new DataBlock();
        blk.setNonce(BigInteger.ONE);
        blk.setPrevBlockHash(new byte[]{0, 1, 2});
        blk.setShard(2);
        blk.setAppStateHash(new byte[]{3, 4, 5});
        blk.setSig1(new byte[]{6, 7, 8});
        blk.setSig2(new byte[]{9, 10, 11});

        List<String> listPubKeys = new ArrayList<String>();
        listPubKeys.add("AAAA");
        listPubKeys.add("BBBB");
        blk.setListPubKeys(listPubKeys);

        List<byte[]> listHashes = new ArrayList<>();
        listHashes.add(new byte[]{12, 13, 14});
        listHashes.add(new byte[]{15, 16, 17});
        listHashes.add(new byte[]{18, 19, 20});
        blk.setListTXHashes(listHashes);
        return blk;
    }
}
