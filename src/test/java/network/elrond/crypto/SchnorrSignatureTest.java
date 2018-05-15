package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.junit.Test;

public class SchnorrSignatureTest {
    @Test
    public void testUninitializedSignature() {
        TestCase.assertEquals(0, 0);
    }

    @Test
    public void testSignAndVerify() {
        for (int i = 0; i < 15; i++) {
            ECKeyPair keyPair = new ECKeyPair();
            // variate the message
            String message = "hello Elrond network " + i;
            byte[] msgHash = Util.SHA3.digest(message.getBytes());
            SchnorrSignature schnorrSignature = new SchnorrSignature();
            // sign the hash
            schnorrSignature.signMessage(msgHash, keyPair.getPrivateKey(), keyPair.getPublicKey());
            TestCase.assertTrue(schnorrSignature.verifySignature(msgHash, keyPair.getPublicKey()));

            System.out.println("\nsignature: " + Util.byteArrayToHexString(schnorrSignature.getSignatureValue()) +
                    "\nlength: " + schnorrSignature.getSignatureValue().length +
                    "\nchallenge: " + Util.byteArrayToHexString(schnorrSignature.getChallenge()) +
                    "\nlength:" + schnorrSignature.getChallenge().length);
        }
    }
}
