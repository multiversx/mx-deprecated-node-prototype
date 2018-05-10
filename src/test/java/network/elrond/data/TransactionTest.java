package network.elrond.data;

import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;

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

        System.out.println(tx.getJSONDataRAW());
        System.out.println(tx.getJSONData());

        System.out.println(tx.decodeJSON(null));
        System.out.println(tx.decodeJSON("{}"));
        System.out.println(tx.decodeJSON("{TX:{}}"));
        System.out.println(tx.decodeJSON("{TX:{nonce:\"aa\"}}"));
        System.out.println(tx.decodeJSON("{TX:{nonce:\"0\",}}"));

        //byte[] buff = "Elrond".getBytes();
        //System.out.println(Base64.encode(buff).toString());
        //System.out.println(new String(Base64.decode(Base64.encode(buff))));


    }



}
