package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;

import java.math.BigInteger;

public class TransactionTest {

    SerializationService serializationService = AppServiceProvider.getSerializationService();

    private String sendAddress ;
    private String recvAddress ;
    private BigInteger nonce;
    private BigInteger value;
    private Shard senderShard;
    private Shard receiverShard;

    @Before
    public void SetupTest(){
        sendAddress = "0xa87b8fa28a8476553363a9356aa02635e4a1b033";
        recvAddress = "0x0000000000000000000000000000000000000000";
        nonce = BigInteger.ZERO;
        value = BigInteger.TEN.pow(8);
        senderShard = AppServiceProvider.getShardingService().getShard(sendAddress.getBytes());
        receiverShard = AppServiceProvider.getShardingService().getShard(recvAddress.getBytes());
    }

    @After
    public void CleanUpTest(){

    }

    @Test
    public void testTransactionConstructorWithCorrectParametersShouldNotThrowException() {

        Transaction tx = new Transaction(sendAddress, recvAddress, value, nonce, senderShard, receiverShard);
        Assert.assertNotNull(tx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendAddressNullShouldThrowException(){
        new Transaction(null, recvAddress, value, nonce, null, receiverShard);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendAddressEmptyShouldThrowException(){
        new Transaction("", recvAddress, value, nonce, senderShard, null);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReceiveAddressNullShouldThrowException(){
        new Transaction(sendAddress, null, value, nonce, senderShard, null);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReceiveAddressEmptyShouldThrowException(){
        new Transaction(sendAddress, "", value, nonce, senderShard, null);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueLessThanZeroShouldThrowException(){
        new Transaction(sendAddress, recvAddress, BigInteger.valueOf(-1), nonce, senderShard, receiverShard);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonceLessThanZeroShouldThrowException(){
        new Transaction(sendAddress, recvAddress, value, BigInteger.valueOf(-1), senderShard, receiverShard);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueNullShouldThrowException(){
        new Transaction(sendAddress, recvAddress, null, nonce, senderShard, receiverShard);
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonceNullZeroShouldThrowException(){
        new Transaction(sendAddress, recvAddress, value, null, senderShard, receiverShard);
        Assert.fail();
    }

    @Test
    public void testTransactionSerialization(){
        Transaction tx = new Transaction(sendAddress, recvAddress, value, nonce, senderShard, receiverShard);

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
    }

    @Test
    public void testHashWithAndWithoutSignature() {
        Transaction tx = new Transaction(sendAddress, recvAddress, value, nonce,senderShard, receiverShard);
        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) i;
        }
        tx.setData(buff);

        tx.setSignature(new byte[]{1, 2, 3});
        tx.setChallenge(new byte[]{4, 5, 6});

        String hashWithSignature = new String(Base64.encode(serializationService.getHash(tx)));

        tx.setChallenge(null);
        tx.setSignature(null);
        String hashWithoutSignature = new String(Base64.encode(serializationService.getHash(tx)));

        System.out.println(hashWithoutSignature);
        System.out.println(hashWithSignature);

        TestCase.assertFalse(hashWithoutSignature == hashWithSignature);
    }
}
