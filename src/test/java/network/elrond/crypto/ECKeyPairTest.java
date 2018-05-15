package network.elrond.crypto;

import junit.framework.TestCase;
import org.junit.Test;

public class ECKeyPairTest {

    @Test
    public void TestKeyPairDefaultConstruction(){
        ECKeyPair keyPair = new ECKeyPair();

        PrivateKey privateKey = keyPair.getPrivateKey();
        PublicKey publicKey = keyPair.getPublicKey();

        // validate the privateKey
        TestCase.assertNotNull(privateKey);
        TestCase.assertTrue(keyPair.getPrivateKey().isValid());

        // validate the public key
        TestCase.assertNotNull(publicKey);
        TestCase.assertTrue(keyPair.getPublicKey().isValid());
    }
}
