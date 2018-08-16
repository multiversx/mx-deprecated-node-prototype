package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class PrivateKeyTest {

    @Test
    public void testDefaultConstructor(){
        PrivateKey privateKey = new PrivateKey();
        Assert.assertNotNull(privateKey.getValue());
    }

    @Test
    public void testConstructorFromByteArray(){
        PrivateKey privateKey = new PrivateKey("test".getBytes());
        Assert.assertNotNull(privateKey.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFromNullByteArray(){
        PrivateKey privateKey = new PrivateKey((byte[])null);
    }

    @Test
    public void testConstructorFromSeed(){
        PrivateKey privateKey = new PrivateKey("Seed");
        Assert.assertNotNull(privateKey.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFromNullSeed(){
        PrivateKey privateKey = new PrivateKey((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFromEmptySeed(){
        PrivateKey privateKey = new PrivateKey("");
    }

    @Test
    public void testGeneratePrivateKeyFromString() {
        String seed = "Lorem ipsum dolor sit amet, ei quo equidem perpetua efficiendi";
        PrivateKey privKey = new PrivateKey(seed);
        System.out.println(Util.byteArrayToHexString(privKey.getValue()));
        System.out.println(Util.byteArrayToHexString(new PublicKey(privKey).getValue()));
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        // validate against checked pair
        TestCase.assertEquals(
                "00948c6246ebb299414ccd3cc8b17674d3f6fe0d14b984b6c2c84e0d5866a38da2",
                Util.byteArrayToHexString(privKey.getValue()));

        // check the random number is within expected range 0 < privateKey < n
        TestCase.assertEquals(1, new BigInteger(1, privKey.getValue()).compareTo(BigInteger.ZERO));
        TestCase.assertEquals(-1, new BigInteger(1, privKey.getValue()).compareTo(ecCryptoService.getN()));
    }
}
