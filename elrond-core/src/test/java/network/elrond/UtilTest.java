package network.elrond;


import junit.framework.TestCase;
import network.elrond.consensus.Validator;
import network.elrond.core.Util;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.util.List;

public class UtilTest {
    public static void displayListValidators(List<Validator> list) {
        for (int i = 0; i < list.size(); i++) {
            Validator v = list.get(i);

            System.out.println(v.getPubKey() + ", S: " + v.getStake().toString(10) + ", R: " + v.getRating());
        }
        System.out.println();
    }

    @Test
    public void testUtilHexToByteArray() {
        TestCase.assertEquals("00", Util.byteArrayToHexString(new byte[]{0}));
        TestCase.assertEquals("0b", Util.byteArrayToHexString(new byte[]{11}));
        TestCase.assertEquals("0f", Util.byteArrayToHexString(new byte[]{15}));
        TestCase.assertEquals("10", Util.byteArrayToHexString(new byte[]{16}));
        TestCase.assertEquals("80", Util.byteArrayToHexString(new byte[]{-128}));
        TestCase.assertEquals("ff", Util.byteArrayToHexString(new byte[]{-1}));
        TestCase.assertEquals("13d18bf84f5643", Util.byteArrayToHexString(new byte[]{19, -47, -117,
        -8, 79, 86, 67}));

        TestCase.assertEquals("13d18bf84f5643", Util.byteArrayToHexString(Util.hexStringToByteArray("13d18bf84f5643")));
    }

    @Test
    public void testUtilGetAddressFromPublicKey() {
        String strPubKeyHexa = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
        String strAddr = "0xa87b8fa28a8476553363a9356aa02635e4a1b033";

        TestCase.assertEquals(strAddr, Util.getAddressFromPublicKey(Util.hexStringToByteArray(strPubKeyHexa)));
    }
}
