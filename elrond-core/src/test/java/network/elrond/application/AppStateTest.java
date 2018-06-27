package network.elrond.application;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.crypto.PrivateKey;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.p2p.P2PConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AppStateTest {
    private AppState appState;

    @Before
    public void SetUp(){
        appState = new AppState();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChannelWithNullChannelNameShouldThrowException(){
        appState.getChanel((P2PBroadcastChannelName) null);
    }

    @Test
    public void testGetChannelWithUnknownChannel(){
        P2PBroadcastChanel channel = appState.getChanel(P2PBroadcastChannelName.BLOCK);
        Assert.assertNull(channel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChannelWithNullChannelShouldThrowException(){
        appState.addChanel((P2PBroadcastChanel) null);
    }

    @Test
    public void testAddChannel(){
        P2PBroadcastChanel test = new P2PBroadcastChanel(P2PBroadcastChannelName.BLOCK, null);
        Assert.assertNull(appState.getChanel(P2PBroadcastChannelName.BLOCK));
        appState.addChanel(test);
        Assert.assertNotNull(appState.getChanel(P2PBroadcastChannelName.BLOCK));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetConnectionWithNullConnectionShouldThrowException(){
        appState.setConnection(null);
    }

    @Test
    public void testSetConnection(){
        P2PConnection connection = new P2PConnection("testConnection", mock(Peer.class), mock(PeerDHT.class));
        appState.setConnection(connection);
        Assert.assertEquals(connection, appState.getConnection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBlockchainWithNullBlockchainShouldThrowException(){
        appState.setBlockchain(null);
    }

    @Test
    public void testSetBlockchain(){
        Blockchain blockchain = mock(Blockchain.class);
        appState.setBlockchain(blockchain);
        Assert.assertEquals(blockchain, appState.getBlockchain());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAccountsWithNullAccountsShouldThrowException(){
        appState.setAccounts(null);
    }

    @Test
    public void testSetAccounts(){
        Accounts accounts = mock(Accounts.class);
        appState.setAccounts(accounts);
        Assert.assertEquals(accounts, appState.getAccounts());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPrivateKeyWithNullPrivateKeyShouldThrowException(){
        appState.setPrivateKey(null);
    }

    @Test
    public void testSetPrivateKey(){
        PrivateKey key = new PrivateKey();
        Assert.assertNull(appState.getPublicKey());
        appState.setPrivateKey(key);
        Assert.assertEquals(key, appState.getPrivateKey());
        Assert.assertNotNull(appState.getPublicKey());
    }

    @Test
    public void testShutdown(){
        Blockchain blockchain = mock(Blockchain.class);
        Accounts accounts = mock(Accounts.class);

        appState.setBlockchain(blockchain);
        appState.setAccounts(accounts);

        verify(blockchain, times(0)).stopPersistenceUnit();
        verify(accounts, times(0)).stopPersistenceUnit();

        appState.shutdown();

        verify(blockchain, times(1)).stopPersistenceUnit();
        verify(accounts, times(1)).stopPersistenceUnit();
    }

}
