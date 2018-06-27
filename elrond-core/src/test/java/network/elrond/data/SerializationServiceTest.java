package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.spongycastle.util.encoders.Base64;
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
    public void testTransactionEncodeDecodeWithSignature(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);
        byte[] signature = new byte[] {(byte)1};
        byte[] challenge = new byte[] {(byte)2};
        tx.setSignature(signature);
        tx.setChallenge(challenge);

        String encodedTransaction = serializationService.encodeJSON(tx);
        //System.out.println(strEncoded);
        Transaction decodedTransaction = serializationService.decodeJSON(encodedTransaction, Transaction.class);

        TestCase.assertEquals(serializationService.encodeJSON(tx), serializationService.encodeJSON(decodedTransaction));
        Assert.assertArrayEquals(signature, decodedTransaction.getSignature());
        Assert.assertArrayEquals(challenge, decodedTransaction.getChallenge());
    }

    @Test
    public void testTransactionEncodeDecodeWithOutSig(){
        Transaction tx = transactionService.generateTransaction(pbKeySender, pbKeyRecv, 0, 0);

        byte[] signature = new byte[] {(byte)1};
        byte[] challenge = new byte[] {(byte)2};
        tx.setSignature(signature);
        tx.setChallenge(challenge);


        tx.setSignature(null);
        tx.setChallenge(null);

        String encodedTransaction = serializationService.encodeJSON(tx);
        //System.out.println(encodedTransaction);
        Transaction decodedTransaction = serializationService.decodeJSON(encodedTransaction, Transaction.class);

        TestCase.assertEquals(serializationService.encodeJSON(tx), serializationService.encodeJSON(decodedTransaction));
        Assert.assertArrayEquals(null, decodedTransaction.getSignature());
        Assert.assertArrayEquals(null, decodedTransaction.getChallenge());
    }

    @Test
    public void testBlkEncodeDecodeWithSig() {
        Block blk = getTestBlock();
        byte[] signature = new byte[] {(byte)1};
        byte[] commitment = new byte[] {(byte)2};
        blk.setSignature(signature);
        blk.setCommitment(commitment);


        String encodedBlock = serializationService.encodeJSON(blk);

        //System.out.println(strEncoded);
        //System.out.println(strEncoded2);

        Block decodedBlock = serializationService.decodeJSON(encodedBlock, Block.class);

        TestCase.assertEquals(encodedBlock, serializationService.encodeJSON(decodedBlock));
        Assert.assertArrayEquals(signature, decodedBlock.getSignature());
        Assert.assertArrayEquals(commitment, decodedBlock.getCommitment());
    }



    @Test
    public void testBlockHashWithSig() {
        Block blk = getTestBlock();

        byte[] signature = new byte[] {(byte)1};
        byte[] commitment = new byte[] {(byte)2};
        blk.setSignature(signature);
        blk.setCommitment(commitment);

        blk.setSignature(null);
        blk.setCommitment(null);
        String encodedBlock = serializationService.encodeJSON(blk);

        //System.out.println(strEncoded);
        //System.out.println(strEncoded2);

        Block decodedBlock = serializationService.decodeJSON(encodedBlock, Block.class);

        TestCase.assertEquals(encodedBlock, serializationService.encodeJSON(decodedBlock));
        Assert.assertArrayEquals(null, decodedBlock.getSignature());
        Assert.assertArrayEquals(null, decodedBlock.getCommitment());
    }

    @Test
    public void testGetHashString() {
        Block blk = getTestBlock();

        String hashString = new String(Base64.encode(serializationService.getHash(blk)));
        String blkHashString = serializationService.getHashString(blk);

        Assert.assertEquals(hashString, blkHashString);
    }

    private Block getTestBlock() {
        Block blk = new Block();
        blk.setNonce(BigInteger.ONE);
        blk.setPrevBlockHash(new byte[]{0, 1, 2});
        blk.setShard(new Shard(0));
        blk.setAppStateHash(new byte[]{3, 4, 5});
        blk.setSignature(new byte[]{6, 7, 8});
        blk.setCommitment(new byte[]{9, 10, 11});

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
