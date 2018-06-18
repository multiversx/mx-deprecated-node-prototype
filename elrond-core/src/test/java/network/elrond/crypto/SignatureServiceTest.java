package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

public class SignatureServiceTest {
    @Test(expected = IllegalArgumentException.class)
    public void testSignNullMessage() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] msg = null;
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        signatureService.signMessage(msg, privateKey, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignEmptyMessage() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        byte[] msg = "".getBytes();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        sigService.signMessage(msg, privateKey, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignNullPrivateKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = null;

        sigService.signMessage("hello", privateKey, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignEmptyPrivateKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = keyPair.getPublicKey().getValue();
        byte[] privateKey = new byte[0];

        sigService.signMessage("hello", privateKey, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignNullPublicKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = null;
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        sigService.signMessage("hello", privateKey, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignEmptyPublicKey() {
        SignatureService sigService = AppServiceProvider.getSignatureService();
        ECKeyPair keyPair = new ECKeyPair();
        byte[] publicKey = new byte[0];
        byte[] privateKey = keyPair.getPrivateKey().getValue();

        sigService.signMessage("hello", privateKey, publicKey);
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

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNullSignature() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = null;
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] publicKey = SHA3Helper.sha3("hello".getBytes());

        signatureService.verifySignature(signature, challenge, message, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNullChallenge() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = SHA3Helper.sha3("dummy signature".getBytes());
        byte[] challenge = null;
        byte[] publicKey = SHA3Helper.sha3("hello".getBytes());

        signatureService.verifySignature(signature, challenge, message, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNullMessage() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = null;
        byte[] signature = SHA3Helper.sha3("dummy signature".getBytes());
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] publicKey = SHA3Helper.sha3("hello".getBytes());

        signatureService.verifySignature(signature, challenge, message, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNullPublicKey() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = SHA3Helper.sha3("dummy signature".getBytes());
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] publicKey = null;

        signatureService.verifySignature(signature, challenge, message, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyEmptySignature() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = new byte[0];
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] publicKey = SHA3Helper.sha3("hello".getBytes());

        signatureService.verifySignature(signature, challenge, message, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyEmptyChallenge() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = SHA3Helper.sha3("dummy signature".getBytes());
        byte[] challenge = new byte[0];
        byte[] publicKey = SHA3Helper.sha3("hello".getBytes());

        signatureService.verifySignature(signature, challenge, message, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyEmptyMessage() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = new byte[0];
        byte[] signature = SHA3Helper.sha3("dummy signature".getBytes());
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] publicKey = SHA3Helper.sha3("hello".getBytes());

        signatureService.verifySignature(signature, challenge, message, publicKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyEmptyPublicKey() {
        SignatureService signatureService = AppServiceProvider.getSignatureService();
        byte[] message = "Hello".getBytes();
        byte[] signature = SHA3Helper.sha3("dummy signature".getBytes());
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] publicKey = new byte[0];

        signatureService.verifySignature(signature, challenge, message, publicKey);
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
            byte[] msgHash = SHA3Helper.sha3(message.getBytes());
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
            byte[] msgHash = SHA3Helper.sha3(message.getBytes());
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
