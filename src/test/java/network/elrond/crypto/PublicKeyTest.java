package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public class PublicKeyTest {
    @Test
    public void testDefautConstructorNotInit() {
        PublicKey publicKey = new PublicKey();

        TestCase.assertFalse(publicKey.isInitialized());
        TestCase.assertNull(publicKey.getQ());
    }

    @Test
    public void testGeneratePublicFromPrivate() {
        // creates a private key
        PrivateKey privk = new PrivateKey("Lorem ipsum dolor sit amet, ei quo equidem perpetua efficiendi");
        // generate associated public key
        PublicKey pubk = new PublicKey(privk);
        System.out.println("generated priv key: " + Util.byteArrayToHexString(privk.getValue()));

        // verify the pair is valid
        TestCase.assertEquals(
                "0302fa311fac6aa56c1a5b08e6c9bcea32fc1939cbef5010c2ab853afb5563976c",
                Util.byteArrayToHexString(pubk.getEncoded()));
    }

    @Test
    public void testEncodeDecode() {
        // creates a private key
        PrivateKey privk = new PrivateKey(
                Util.hexStringToByteArray("948c6246ebb299414ccd3cc8b17674d3f6fe0d14b984b6c2c84e0d5866a38da2"));
        //Generate associated public key
        PublicKey pubk = new PublicKey(privk);
        PublicKey pubk2 = new PublicKey();

        try {
            // sets public key ECPoint from encoded byte array
            pubk2.setPublicKey(pubk.getEncoded());

            // test encoded form form both public keys is the same
            TestCase.assertEquals(
                    "021a50a33eb266ace1597f4399086b15b989d5c303dfc4ada06454dd7325062286",
                    Util.byteArrayToHexString(pubk2.getEncoded()));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            TestCase.fail(e.toString());
        }
    }
}
