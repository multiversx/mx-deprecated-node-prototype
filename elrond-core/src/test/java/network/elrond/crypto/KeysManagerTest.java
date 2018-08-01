package network.elrond.crypto;

import org.junit.Test;

public class KeysManagerTest {
    @Test
    public void testGenerateNextPrivateKey(){
        KeysManager mng = KeysManager.getInstance();
        String pkey = mng.getNextPrivateKey("");
        pkey = mng.getNextPrivateKey("");
        pkey = mng.getNextPrivateKey("");
    }
}
