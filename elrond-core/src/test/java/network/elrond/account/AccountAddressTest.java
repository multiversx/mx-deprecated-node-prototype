package network.elrond.account;

import network.elrond.core.Util;
import org.junit.Assert;
import org.junit.Test;

public class AccountAddressTest {

    @Test
    public void testAccountAddressAreEqualWhateverMethodYouUse(){
        byte[] addrBytes = Util.PUBLIC_KEY_MINTING.getValue();

        AccountAddress address = AccountAddress.fromBytes(addrBytes);
        AccountAddress addressFromPublicKey = AccountAddress.fromBytes(addrBytes);
        AccountAddress addressFromHexString = AccountAddress.fromHexString(Util.byteArrayToHexString(addrBytes));
        AccountAddress addressFromBytes = AccountAddress.fromBytes(addrBytes);

        Assert.assertArrayEquals(address.getBytes(), addressFromPublicKey.getBytes());
        Assert.assertArrayEquals(addressFromPublicKey.getBytes(), addressFromHexString.getBytes());
        Assert.assertArrayEquals(addressFromPublicKey.getBytes(), addressFromBytes.getBytes());


//        TestCase.assertEquals(Util.getAddressFromPublicKey(Util.PUBLIC_KEY_MINTING.getValue()),
//                Util.byteArrayToHexString(addrBytes));
//
//        System.out.println(Util.getAddressFromPublicKey(Util.PUBLIC_KEY_MINTING.getValue()));
    }

    @Test
    public void testAccountAddressFromBytes(){
        AccountAddress accountAddress = AccountAddress.fromBytes(Util.PUBLIC_KEY_MINTING.getValue());
        Assert.assertTrue(accountAddress!=null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountAddressFromNullBytesShouldThrowException(){
        AccountAddress accountAddress = AccountAddress.fromBytes(null);
        Assert.fail();
    }

    @Test
    public void testAccountAddressFromHexaString(){
        AccountAddress accountAddress = AccountAddress.fromHexString(Util.byteArrayToHexString(Util.PUBLIC_KEY_MINTING.getValue()));
        Assert.assertTrue(accountAddress!=null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountAddressFromNullHexaStringShouldThrowException(){
        AccountAddress accountAddress = AccountAddress.fromHexString(null);
        Assert.fail();
    }

}
