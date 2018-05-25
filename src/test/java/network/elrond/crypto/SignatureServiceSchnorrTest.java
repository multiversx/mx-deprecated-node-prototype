package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

public class SignatureServiceSchnorrTest {
    @Test
    public void testSignNullMessage() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] msg = null;
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        try {
            signatureService.signMessage(msg, privateKey, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (IllegalArgumentException ex) {
            TestCase.assertEquals("message != null", ex.getMessage());
        }
    }

    @Test
    public void testSignEmptyMessage() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        byte[] msg = "".getBytes();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        try {
            sigService.signMessage(msg, privateKey, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (IllegalArgumentException ex) {
            TestCase.assertEquals("message.length != 0", ex.getMessage());
        }
    }

    @Test
    public void testSignNullPrivateKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = null;

        try {
            sigService.signMessage("hello", privateKey, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (IllegalArgumentException ex) {
            TestCase.assertEquals("privateKey != null", ex.getMessage());
        }
    }

    @Test
    public void testSignEmptyPrivateKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = new byte[0];

        try {
            sigService.signMessage("hello", privateKey, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (IllegalArgumentException ex) {
            TestCase.assertEquals("privateKey.length != 0", ex.getMessage());
        }
    }

    @Test
    public void testSignNullPublicKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = null;
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        try {
            sigService.signMessage("hello", privateKey, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (IllegalArgumentException ex) {
            TestCase.assertEquals("publicKey != null", ex.getMessage());
        }
    }

    @Test
    public void testSignEmptyPublicKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = new byte[0];
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        try {
            sigService.signMessage("hello", privateKey, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (IllegalArgumentException ex) {
            TestCase.assertEquals("publicKey.length != 0", ex.getMessage());
        }
    }

    @Test
    public void testSignVerifyKeysNotMatching() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        Signature sig;
        ECKeyPair keyPair = new ECKeyPair();
        ECKeyPair keyPair2 = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = keyPair.getPrivateKey().getValue();
        byte[] msg = "hello Elrond network".getBytes();
        byte[] publicKey2 = keyPair2.getPublicKey().getValue();

        sig = sigService.signMessage(msg, privateKey, publicKey);
        TestCase.assertFalse(sigService.verifySignature(sig.getSignature(), sig.getChallenge(), msg, publicKey2));
    }

    @Test
    public void testVerifyNullSignature() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = null;
        byte[] challenge = Util.SHA3.digest("dummy challenge".getBytes());
        byte[] publicKey = Util.SHA3.digest("hello".getBytes());

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("signature != null", ex.getMessage());
        }
    }

    @Test
    public void testVerifyNullChallenge() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = Util.SHA3.digest("dummy signature".getBytes());
        byte[] challenge = null;
        byte[] publicKey = Util.SHA3.digest("hello".getBytes());

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("challenge != null", ex.getMessage());
        }
    }

    @Test
    public void testVerifyNullMessage() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = null;
        byte[] signature = Util.SHA3.digest("dummy signature".getBytes());
        byte[] challenge = Util.SHA3.digest("dummy challenge".getBytes());
        ;
        byte[] publicKey = Util.SHA3.digest("hello".getBytes());

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("message != null", ex.getMessage());
        }
    }

    @Test
    public void testVerifyNullPublicKey() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = Util.SHA3.digest("dummy signature".getBytes());
        byte[] challenge = Util.SHA3.digest("dummy challenge".getBytes());
        ;
        byte[] publicKey = null;

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("publicKey != null", ex.getMessage());
        }
    }

    @Test
    public void testVerifyEmptySignature() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = new byte[0];
        byte[] challenge = Util.SHA3.digest("dummy challenge".getBytes());
        byte[] publicKey = Util.SHA3.digest("hello".getBytes());

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("signature.length != 0", ex.getMessage());
        }
    }

    @Test
    public void testVerifyEmptyChallenge() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = Util.SHA3.digest("dummy signature".getBytes());
        byte[] challenge = new byte[0];
        byte[] publicKey = Util.SHA3.digest("hello".getBytes());

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("challenge.length != 0", ex.getMessage());
        }
    }

    @Test
    public void testVerifyEmptyMessage() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = new byte[0];
        byte[] signature = Util.SHA3.digest("dummy signature".getBytes());
        byte[] challenge = Util.SHA3.digest("dummy challenge".getBytes());
        ;
        byte[] publicKey = Util.SHA3.digest("hello".getBytes());

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("message.length != 0", ex.getMessage());
        }
    }

    @Test
    public void testVerifyEmptyPublicKey() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = Util.SHA3.digest("dummy signature".getBytes());
        byte[] challenge = Util.SHA3.digest("dummy challenge".getBytes());
        ;
        byte[] publicKey = new byte[0];

        try {
            signatureService.verifySignature(signature, challenge, message, publicKey);
            TestCase.fail("Exception expected but not thrown");
        } catch (Exception ex) {
            TestCase.assertEquals("publicKey.length != 0", ex.getMessage());
        }
    }

    @Test
    public void testSignAndVerifyByteArrayMessage() {
        System.out.println("Testing single signatures");
        for (int i = 0; i < 15; i++) {
            ECKeyPair keyPair = new ECKeyPair();
            byte[] publicKey = keyPair.getPublicKey().getValue();
            byte[] privateKey = keyPair.getPrivateKey().getValue();
            // variate the message
            String message = "hello Elrond network " + i;
            byte[] msgHash = Util.SHA3.digest(message.getBytes());
            SignatureService signatureService = AppServiceProvider.getSignatureService();
            Signature sig;

            // sign the hash
            sig = signatureService.signMessage(msgHash, privateKey, publicKey);
            System.out.println("signature: " + Util.byteArrayToHexString(sig.getSignature()));
            System.out.println("commitment: " + Util.byteArrayToHexString(sig.getCommitment()));
            System.out.println("challenge: " + Util.byteArrayToHexString(sig.getChallenge()));
            System.out.println();
            TestCase.assertTrue(signatureService.verifySignature(sig.getSignature(), sig.getChallenge(), msgHash, publicKey));
        }
    }

    @Test
    public void testSignAndVerifyStringMessage() {
        System.out.println("Testing single signatures");
        for (int i = 0; i < 15; i++) {
            ECKeyPair keyPair = new ECKeyPair();
            byte[] publicKey = keyPair.getPublicKey().getValue();
            byte[] privateKey = keyPair.getPrivateKey().getValue();
            // variate the message
            String message = ("hello Elrond network " + i);
            byte[] msgHash = Util.SHA3.digest(message.getBytes());
            String msgHashString = Util.byteArrayToHexString(msgHash);
            SignatureService signatureService = AppServiceProvider.getSignatureService();
            Signature sig;

            // sign the hash
            sig = signatureService.signMessage(msgHashString, privateKey, publicKey);
            System.out.println("signature: " + Util.byteArrayToHexString(sig.getSignature()));
            System.out.println("commitment: " + Util.byteArrayToHexString(sig.getCommitment()));
            System.out.println("challenge: " + Util.byteArrayToHexString(sig.getChallenge()));
            System.out.println();
            TestCase.assertTrue(signatureService.verifySignature(sig.getSignature(), sig.getChallenge(), msgHashString, publicKey));
        }
    }
}
