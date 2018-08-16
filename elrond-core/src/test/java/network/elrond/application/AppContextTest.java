package network.elrond.application;

import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.BootstrapType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppContextTest {
    AppContext context;

    @Before
    public void SetUp(){
        context = new AppContext();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNodeNameWithNull(){
        context.setNodeName("");
    }

    @Test
    public void testSetAndGetNodeName(){
        String nodeName = "testName";
        context.setNodeName(nodeName);
        Assert.assertEquals(nodeName,context.getNodeName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPortWithNegativeValueShoudlThrowException(){
        context.setPort(-2);
    }

    @Test
    public void testSetAndGetPort(){
        Integer port = 10;
        context.setPort(port);
        Assert.assertEquals(port, context.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMasterPeerIpAddressWithNullValueShouldThrowException(){
        context.setMasterPeerIpAddress("");
    }

    @Test
    public void testSetMasterPeerIpAddress(){
        String masterPeerIp = "ip";
        context.setMasterPeerIpAddress(masterPeerIp);
        Assert.assertEquals(masterPeerIp, context.getMasterPeerIpAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMasterPeerPortWithNegativeValueShoudlThrowException(){
        context.setMasterPeerPort(-2);
    }

    @Test
    public void testSetMasterPeerAndGetMasterPeerPort(){
        Integer port = 10;
        context.setMasterPeerPort(port);
        Assert.assertEquals(port, context.getMasterPeerPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStorageBasePathWithNullValueShouldThrowException(){
        context.setStorageBasePath("");
    }

    @Test
    public void testSetStorageBasePath(){
        String storageBasePath = "path";
        context.setStorageBasePath(storageBasePath);
        Assert.assertEquals(storageBasePath, context.getStorageBasePath());
    }

    @Test
    public void testSetBootstrapType(){
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);
        Assert.assertEquals(BootstrapType.START_FROM_SCRATCH, context.getBootstrapType());
        context.setBootstrapType(BootstrapType.REBUILD_FROM_DISK);
        Assert.assertEquals(BootstrapType.REBUILD_FROM_DISK, context.getBootstrapType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStrAddressMintWithNullValueShouldThrowException(){
        context.setStrAddressMint("");
    }

    @Test
    public void testSetStrAddressMint(){
        String addressMint = "path";
        context.setStrAddressMint(addressMint);
        Assert.assertEquals(addressMint, context.getStrAddressMint());
    }

    @Test
    public void testGetValueMint(){
        Assert.assertEquals(0, Util.VALUE_MINTING.compareTo(context.getValueMint()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPrivateKeyWithNullValueShouldThrowException(){
        context.setPrivateKey(null);
    }

    @Test
    public void testSetPrivateKey(){
        PrivateKey privateKey = new PrivateKey();
        context.setPrivateKey(privateKey);
        Assert.assertEquals(privateKey, context.getPrivateKey());
    }
}
