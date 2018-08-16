package network.elrond.account;

import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.junit.Assert;
import org.junit.Test;

public class AccountAddressTest {

    @Test
    public void testAccountAddressAreEqualWhateverMethodYouUse() {
        byte[] addrBytes = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0)).getValue();

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
    public void testAccountAddressFromBytes() {
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        AccountAddress accountAddress = AccountAddress.fromBytes(publicKeyMinting.getValue());
        Assert.assertNotNull(accountAddress);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountAddressFromNullBytesShouldThrowException() {
        AccountAddress accountAddress = AccountAddress.fromBytes(null);
        Assert.fail();
    }

    @Test
    public void testAccountAddressFromHexaString() {
        PublicKey publicKeyMinting = AppServiceProvider.getShardingService().getPublicKeyForMinting(new Shard(0));
        AccountAddress accountAddress = AccountAddress.fromHexString(Util.byteArrayToHexString(publicKeyMinting.getValue()));
        Assert.assertTrue(accountAddress != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccountAddressFromNullHexaStringShouldThrowException() {
        AccountAddress accountAddress = AccountAddress.fromHexString(null);
        Assert.fail();
    }

}
