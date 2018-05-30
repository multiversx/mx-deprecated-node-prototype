package network.elrond.account;

import junit.framework.TestCase;
import network.elrond.core.Util;
import org.junit.Test;

public class AccountAddressTest {

    @Test
    public void testAccountAddress(){
        byte[] addrBytes = Util.PUBLIC_KEY_MINTING.getValue();

        TestCase.assertEquals(Util.getAddressFromPublicKey(Util.PUBLIC_KEY_MINTING.getValue()),
                Util.byteArrayToHexString(addrBytes));

        System.out.println(Util.getAddressFromPublicKey(Util.PUBLIC_KEY_MINTING.getValue()));




    }
}
