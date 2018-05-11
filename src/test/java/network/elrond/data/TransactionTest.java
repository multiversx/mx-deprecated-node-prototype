package network.elrond.data;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runners.model.TestClass;

public class TransactionTest {
    @Test
    public void testBlock() {

        Transaction tx = new Transaction();
        byte[] buff = new byte[100];
        for (int i = 0; i < buff.length; i++)
        {
            buff[i] = (byte)i;
        }
        tx.setData(buff);

        System.out.println(tx.encodeJSONnoSig());
        System.out.println(tx.encodeJSON());

        Transaction tx2 = new Transaction();
        tx2.decodeJSON(tx.encodeJSON());
        System.out.println(tx2.encodeJSON());

        TestCase.assertEquals(tx.encodeJSON(), tx2.encodeJSON());

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
