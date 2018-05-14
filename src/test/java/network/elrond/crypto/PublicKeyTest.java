package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public class PublicKeyTest {
    @Test
    public void testGeneratePublicFromPrivate() {
        // creates a private key
        PrivateKey privk = new PrivateKey("Lorem ipsum dolor sit amet, ei quo equidem perpetua efficiendi");
        // generate associated public key
        PublicKey pubk = new PublicKey(privk);
        // verify the pair is valid
        TestCase.assertEquals(
                "021a50a33eb266ace1597f4399086b15b989d5c303dfc4ada06454dd7325062286",
                Util.byteArrayToHexString(pubk.getEncoded()));
    }

    @Test
    public void testEncodeDecode() {
        // creates a private key
        PrivateKey privk = new PrivateKey(
                Util.hexStringToByteArray("00885c40457367723ac0cb3b6d7cb63aca1a978753f6aa520aef98f58165163d2b"));
        //Generate associated public key
        PublicKey pubk = new PublicKey(privk);
        PublicKey pubk2 = new PublicKey();

        try {
            // sets public key ECPoint from encoded byte array
            pubk2.setPublicKey(pubk.getEncoded());

            // test encoded form form both public keys is the same
            TestCase.assertEquals(
                    "026ef9fd8f0e665a17169235ca0f68bb51d46d65f5816d1e184f858872bfbf62be",
                    Util.byteArrayToHexString(pubk2.getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            TestCase.fail(e.toString());
        } catch (NoSuchProviderException e) {
            TestCase.fail(e.toString());
        } catch (InvalidKeySpecException e) {
            TestCase.fail(e.toString());
        }
    }
}
