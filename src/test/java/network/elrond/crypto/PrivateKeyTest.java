package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.junit.Test;

public class PrivateKeyTest {
    @Test
    public void testGeneratePrivateKeyFromString() {
        String seed = "Lorem ipsum dolor sit amet, ei quo equidem perpetua efficiendi";
        PrivateKey privKey = new PrivateKey(seed);

        TestCase.assertEquals(
                "e90afa591609b87eb394b1e8ab841c280bf32a58203f9ffa5ee3b11f4e493fd6",
                Util.byteArrayToHexString(privKey.getPrivateKey().toByteArray()));
    }
}
