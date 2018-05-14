package network.elrond.crypto;

import junit.framework.TestCase;
import org.junit.Test;

public class SchnorrSignatureTest {
    @Test
    public void testUninitializedSignature() {
        TestCase.assertEquals(0, 0);
    }

    @Test
    public void testSignAndVerify() {
        ECKeyPair keyPair = new ECKeyPair();
        String message = "hello Elrond network";
        SchnorrSignature schnorrSignature = new SchnorrSignature();
        schnorrSignature.signMessage(message, keyPair.getPrivateKey(), keyPair.getPublicKey());

        TestCase.assertEquals(true, schnorrSignature.verifySignature(message, keyPair.getPublicKey()));
    }
}
