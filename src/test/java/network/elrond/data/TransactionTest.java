package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.math.BigInteger;

public class TransactionTest {
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
        tx.setSig(Util.hexStringToByteArray("00"));
        tx.setSendAddress("0xa87b8fa28a8476553363a9356aa02635e4a1b033");
        tx.setRecvAddress("0x0000000000000000000000000000000000000000");
        tx.setNonce(BigInteger.ZERO);
        tx.setValue(BigInteger.TEN.pow(8)); //1 ERD

        System.out.println(tx.encodeJSONnoSig());
        System.out.println(tx.encodeJSON());

        System.out.println(tx.encodeJSON().length());

        Transaction tx2 = new Transaction();
        tx2.decodeJSON(tx.encodeJSON());
        System.out.println(tx2.encodeJSON());

        //test encode-decode
        TestCase.assertEquals(tx.encodeJSON(), tx2.encodeJSON());

        //test encode-recreate
        Transaction tx3 = Transaction.createTransaction(tx.encodeJSON());

        if (tx3 != null) {
            System.out.println(tx3.encodeJSON());
        } else {
            System.out.println("NULL");
        }

        TestCase.assertEquals(tx3.encodeJSON(), tx.encodeJSON());




//        System.out.println(tx.decodeJSON(null));
//        System.out.println(tx.decodeJSON("{}"));
//        System.out.println(tx.decodeJSON("{TX:{}}"));
//        System.out.println(tx.decodeJSON("{TX:{nonce:\"aa\"}}"));
//        System.out.println(tx.decodeJSON("{TX:{nonce:\"0\",}}"));



        //byte[] buff = "Elrond".getBytes();
        //System.out.println(Base64.encode(buff).toString());
        //System.out.println(new String(Base64.decode(Base64.encode(buff))));


    }



}
