package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.junit.Test;

import java.math.BigInteger;

public class PrivateKeyTest {
    @Test
    public void testGeneratePrivateKeyFromString() {
        String seed = "Lorem ipsum dolor sit amet, ei quo equidem perpetua efficiendi";
        PrivateKey privKey = new PrivateKey(seed);
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
