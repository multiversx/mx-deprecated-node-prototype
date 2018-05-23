package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
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
            SignatureService signatureService = AppServiceProvider.getSignatureService();
            Signature sig;
            // sign the hash
            sig = signatureService.signMessage(msgHash, keyPair.getPrivateKey(), keyPair.getPublicKey());
            TestCase.assertTrue(signatureService.verifySignature(sig.getSignature(), sig.getChallenge(), msgHash, keyPair.getPublicKey()));
        }
    }
}
