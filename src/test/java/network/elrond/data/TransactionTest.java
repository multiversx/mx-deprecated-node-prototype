package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.math.BigInteger;

public class TransactionTest {
    TransactionService ts = AppServiceProvider.getTransactionService();

    @Test
    public void testTransaction() {

        Transaction tx = new Transaction();
        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++)
        {
            buff[i] = (byte)i;
        }
        tx.setData(buff);
        tx.setPubKey("025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae");
        tx.setSig1(Util.hexStringToByteArray("00"));
        tx.setSendAddress("0xa87b8fa28a8476553363a9356aa02635e4a1b033");
        tx.setRecvAddress("0x0000000000000000000000000000000000000000");
        tx.setNonce(BigInteger.ZERO);
        tx.setValue(BigInteger.TEN.pow(8)); //1 ERD

        System.out.println(ts.encodeJSON(tx, false));
        System.out.println(ts.encodeJSON(tx, true));

        System.out.println(ts.encodeJSON(tx, true).length());

        Transaction tx2 = ts.decodeJSON(ts.encodeJSON(tx, true));
        System.out.println(ts.encodeJSON(tx2, true));

        //test encode-decode
        TestCase.assertEquals(ts.encodeJSON(tx, true), ts.encodeJSON(tx2, true));

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
    public void signTransaction(){
        PrivateKey pvKey = new PrivateKey();
        PublicKey pbKey = new PublicKey(pvKey);

        Transaction tx = new Transaction();
        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++)
        {
            buff[i] = (byte)i;
        }
        tx.setData(buff);
        tx.setPubKey(Util.byteArrayToHexString(pbKey.getEncoded()));
        tx.setSendAddress(Util.getAddressFromPublicKey(pbKey.getEncoded()));
        tx.setRecvAddress("0x0000000000000000000000000000000000000000");
        tx.setNonce(BigInteger.ZERO);
        tx.setValue(BigInteger.TEN.pow(8)); //1 ERD


        ts.signTransaction(tx, pvKey.getValue().toByteArray());

        System.out.println(ts.encodeJSON(tx, true));

        Transaction tx2 = ts.decodeJSON(ts.encodeJSON(tx, true));
        tx2.setGasLimit(BigInteger.ONE);

        System.out.println(ts.encodeJSON(tx2, true));

        TestCase.assertEquals(true, ts.verifyTransaction(tx));
        TestCase.assertEquals(false, ts.verifyTransaction(tx2));
    }

}
