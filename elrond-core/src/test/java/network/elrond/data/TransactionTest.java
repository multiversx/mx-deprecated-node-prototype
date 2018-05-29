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
import java.util.Arrays;

public class TransactionTest {

    SerializationService serializationService = AppServiceProvider.getSerializationService();
    TransactionService transactionService = AppServiceProvider.getTransactionService();

    private String sendAddress ;
    private String recvAddress ;
    private String pubKey ;
    private BigInteger nonce;
    private BigInteger value;

    @Before
    public void SetupTest(){
        sendAddress = "0xa87b8fa28a8476553363a9356aa02635e4a1b033";
        recvAddress = "0x0000000000000000000000000000000000000000";
        pubKey = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
        nonce = BigInteger.ZERO;
        value = BigInteger.TEN.pow(8);
    }

    @After
    public void CleanUpTest(){

    }

    @Test
    public void testTransactionConstructorWithCorrectParametersShouldNotThrowException() {
        Transaction tx = new Transaction(sendAddress, recvAddress, value, nonce);
        Assert.assertNotNull(tx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendAddressNullShouldThrowException(){
        Transaction tx = new Transaction(null, recvAddress, value, nonce);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendAddressEmptyShouldThrowException(){
        Transaction tx = new Transaction("", recvAddress, value, nonce);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReceiveAddressNullShouldThrowException(){
        Transaction tx = new Transaction(sendAddress, null, value, nonce);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReceiveAddressEmptyShouldThrowException(){
        Transaction tx = new Transaction(sendAddress, "", value, nonce);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueLessThanZeroShouldThrowException(){
        Transaction tx = new Transaction(sendAddress, recvAddress, BigInteger.valueOf(-1), nonce);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonceLessThanZeroShouldThrowException(){
        Transaction tx = new Transaction(sendAddress, recvAddress, value, BigInteger.valueOf(-1));
        Assert.fail();
    }

    @Test
    public void testTransactionSerialization(){
        Transaction tx = new Transaction(sendAddress, recvAddress, value, nonce);

        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) i;
        }

        tx.setData(buff);

        tx.setSignature(Util.hexStringToByteArray("00"));
        tx.setReceiverAddress("0x0000000000000000000000000000000000000000");
        tx.setNonce(BigInteger.ZERO);
        tx.setValue(BigInteger.TEN.pow(8)); //1 ERD

        System.out.println(serializationService.encodeJSON(tx));
        System.out.println(serializationService.encodeJSON(tx));

        System.out.println(serializationService.encodeJSON(tx).length());

        Transaction tx2 = serializationService.decodeJSON(serializationService.encodeJSON(tx), Transaction.class);
        System.out.println(serializationService.encodeJSON(tx2));

        //test encode-decode
        TestCase.assertEquals(serializationService.encodeJSON(tx), serializationService.encodeJSON(tx2));

        //TestCase.assertEquals(tx3.encodeJSON(), tx.encodeJSON());


//        System.out.println(tx.decodeJSON(null));
//        System.out.println(tx.decodeJSON("{}"));
//        System.out.println(tx.decodeJSON("{TX:{}}"));
//        System.out.println(tx.decodeJSON("{TX:{nonce:\"aa\"}}"));
//        System.out.println(tx.decodeJSON("{TX:{nonce:\"0\",}}"));


        //byte[] buff = "Elrond".getBytes();
        //System.out.println(Base64.encode(buff).toString());
        //System.out.println(new String(Base64.decode(Base64.encode(buff))));
    }

    @Test
    public void testHash(){
        PrivateKey pvKey = new PrivateKey();
        PublicKey pbKey = new PublicKey(pvKey);

        Transaction tx = new Transaction(sendAddress, recvAddress, value, nonce);
        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) i;
        }
        tx.setData(buff);
        tx.setPubKey(Util.byteArrayToHexString(pbKey.getValue()));
        tx.setSendAddress(Util.getAddressFromPublicKey(pbKey.getValue()));
        tx.setReceiverAddress("0x0000000000000000000000000000000000000000");
        tx.setNonce(BigInteger.ZERO);
        tx.setValue(BigInteger.TEN.pow(8)); //1 ERD


        tx.setSignature(new byte[]{1, 2, 3});
        tx.setChallenge(new byte[]{4, 5, 6});

        System.out.println(new String(Base64.encode(serializationService.getHash(tx))));
        System.out.println(new String(Base64.encode(serializationService.getHash(tx))));

        TestCase.assertEquals(false, Arrays.equals(serializationService.getHash(tx), serializationService.getHash(tx)));

    }

    @Test
    public void signTransaction() {
        PrivateKey pvKey = new PrivateKey();
        PublicKey pbKey = new PublicKey(pvKey);

        PrivateKey pvKey1 = new PrivateKey();
        PublicKey pbKey1 = new PublicKey(pvKey1);

        Transaction tx = new Transaction(Util.getAddressFromPublicKey(pbKey.getValue()),
                Util.getAddressFromPublicKey(pbKey1.getValue()), value, nonce);

        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) i;
        }
        tx.setData(buff);
        tx.setPubKey(Util.byteArrayToHexString(pbKey.getValue()));

        transactionService.signTransaction(tx, pvKey.getValue());

        System.out.println(serializationService.encodeJSON(tx));

        Transaction tx2 = serializationService.decodeJSON(serializationService.encodeJSON(tx), Transaction.class);
        tx2.setGasLimit(BigInteger.ONE);

        System.out.println(serializationService.encodeJSON(tx2));
        TestCase.assertTrue(transactionService.verifyTransaction(tx));
        TestCase.assertFalse(transactionService.verifyTransaction(tx2));

    }

}
