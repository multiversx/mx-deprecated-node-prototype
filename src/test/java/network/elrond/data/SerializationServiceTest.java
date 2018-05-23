package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SerializationServiceTest {
    private PrivateKey pvKeySender = new PrivateKey("a");
    private PublicKey pbKeySender = new PublicKey(pvKeySender);

    private PrivateKey pvKeyRecv = new PrivateKey("b");
    private PublicKey pbKeyRecv = new PublicKey(pvKeyRecv);

    @Test
    public void txSerializationTest() {
        SerializationService serializationService = AppServiceProvider.getSerializationService();
        TransactionService transactionService = AppServiceProvider.getTransactionService();

        Transaction tx = generateTransaction(0, transactionService);

        String strEncoded = serializationService.encodeJSON(tx);

        System.out.println(strEncoded);

        Transaction tx2 = serializationService.decodeJSON(strEncoded, Transaction.class);

        TestCase.assertEquals(serializationService.encodeJSON(tx), serializationService.encodeJSON(tx2));
    }

    @Test
    public void blkSerializationTest() {
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

        SerializationService serv = AppServiceProvider.getSerializationService();
        BlockService blkServ = AppServiceProvider.getBlockService();

        String strEncoded = serv.encodeJSON(blk);

        System.out.println(strEncoded);

        Block blk2 = serv.decodeJSON(strEncoded, DataBlock.class);

        TestCase.assertEquals(AppServiceProvider.getSerializationService().encodeJSON(blk), AppServiceProvider.getSerializationService().encodeJSON(blk2));
    }


    private Transaction generateTransaction(int value, TransactionService trxServ) {
        Transaction tx = new Transaction();
        tx.setNonce(BigInteger.ZERO);
        //2 ERDs
        tx.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(value)));
        tx.setSendAddress(Util.getAddressFromPublicKey(pbKeySender.getEncoded()));
        tx.setReceiverAddress(Util.getAddressFromPublicKey(pbKeyRecv.getEncoded()));
        tx.setPubKey(Util.byteArrayToHexString(pbKeySender.getEncoded()));

        trxServ.signTransaction(tx, pvKeySender.getValue());

        return (tx);
    }
}
