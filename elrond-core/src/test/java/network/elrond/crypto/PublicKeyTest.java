package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.junit.Assert;
import org.junit.Test;

public class PublicKeyTest {

    @Test
    public void testConstructorWithPrivateKeyInitializezPublicKey(){
        PublicKey publicKey = new PublicKey(new PrivateKey("Test"));
        Assert.assertNotNull(publicKey);
        Assert.assertTrue(publicKey.isInitialized());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithPrivateKeyNullThrowsException(){
        PublicKey publicKey = new PublicKey((PrivateKey) null);
    }

    @Test
    public void testConstructorWithPublicKeyEncoding(){
        PublicKey publicKey = new PublicKey(new PrivateKey("Test"));
        PublicKey createdKey = new PublicKey(publicKey.getValue());
        Assert.assertNotNull(createdKey);
        Assert.assertTrue(createdKey.isInitialized());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithPublicKeyEncodingNullShouldThrowException(){
        PublicKey publicKey = new PublicKey((byte[]) null);
    }

    @Test
    public void testConstructorWithPublicKey(){
        PublicKey pbKey = new PublicKey(new PrivateKey("Test"));
        PublicKey generatedPbKey = new PublicKey(pbKey);

        Assert.assertTrue(generatedPbKey.isInitialized());
        Assert.assertArrayEquals(pbKey.getValue(), generatedPbKey.getValue());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructorWithPublicKeyNullShouldThrowException(){
        PublicKey generatedPbKey = new PublicKey((PublicKey)null);
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
                Util.byteArrayToHexString(pubk.getQ().getEncoded(true)));
    }

    @Test
    public void testEncodeDecode() {
        // creates a private key
        PrivateKey privk = new PrivateKey(
                Util.hexStringToByteArray("948c6246ebb299414ccd3cc8b17674d3f6fe0d14b984b6c2c84e0d5866a38da2"));
        //Generate associated public key
        PublicKey pubk = new PublicKey(privk);
        PublicKey pubk2 = new PublicKey(pubk);

        try {
            // sets public key ECPoint from encoded byte array
            //pubk2.setPublicKey(pubk.getQ().getEncoded(true));

            // test encoded form form both public keys is the same
            TestCase.assertEquals(
                    "021a50a33eb266ace1597f4399086b15b989d5c303dfc4ada06454dd7325062286",
                    Util.byteArrayToHexString(pubk2.getQ().getEncoded(true)));
        } catch (Exception e) {
            TestCase.fail(e.toString());
        }
    }
}
