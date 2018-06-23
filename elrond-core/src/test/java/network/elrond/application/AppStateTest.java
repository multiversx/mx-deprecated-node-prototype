package network.elrond.application;

import junit.framework.TestCase;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.core.ThreadUtil;
import network.elrond.crypto.PrivateKey;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
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
    public void SetUp() {
        appState = new AppState();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChannelWithNullChannelNameShouldThrowException() {
        appState.getChanel(null);
    }

    @Test
    public void testGetChannelWithUnknownChannel() {
        P2PBroadcastChanel channel = appState.getChanel(P2PChannelName.BLOCK);
        Assert.assertNull(channel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChannelWithNullChannelShouldThrowException() {
        appState.addChanel(null);
    }

    @Test
    public void testAddChannel() {
        P2PBroadcastChanel test = new P2PBroadcastChanel(P2PChannelName.BLOCK, null);
        Assert.assertNull(appState.getChanel(P2PChannelName.BLOCK));
        appState.addChanel(test);
        Assert.assertNotNull(appState.getChanel(P2PChannelName.BLOCK));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetConnectionWithNullConnectionShouldThrowException() {
        appState.setConnection(null);
    }

    @Test
    public void testSetConnection() {
        P2PConnection connection = new P2PConnection("testConnection", mock(Peer.class), mock(PeerDHT.class));
        appState.setConnection(connection);
        Assert.assertEquals(connection, appState.getConnection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBlockchainWithNullBlockchainShouldThrowException() {
        appState.setBlockchain(null);
    }

    @Test
    public void testSetBlockchain() {
        Blockchain blockchain = mock(Blockchain.class);
        appState.setBlockchain(blockchain);
        Assert.assertEquals(blockchain, appState.getBlockchain());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAccountsWithNullAccountsShouldThrowException() {
        appState.setAccounts(null);
    }

    @Test
    public void testSetAccounts() {
        Accounts accounts = mock(Accounts.class);
        appState.setAccounts(accounts);
        Assert.assertEquals(accounts, appState.getAccounts());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPrivateKeyWithNullPrivateKeyShouldThrowException() {
        appState.setPrivateKey(null);
    }

    @Test
    public void testSetPrivateKey() {
        PrivateKey key = new PrivateKey();
        Assert.assertNull(appState.getPublicKey());
        appState.setPrivateKey(key);
        Assert.assertEquals(key, appState.getPrivateKey());
        Assert.assertNotNull(appState.getPublicKey());
    }

    @Test
    public void testShutdown() {
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

//    @Test
//    public void testLock() {
//        Assert.assertFalse(appState.isLock());
//        appState.acquireLock();
//        Assert.assertTrue(appState.isLock());
//        appState.releaseLock();
//        Assert.assertFalse(appState.isLock());
//    }

//    @Test
//    public void testConcurrencyTestOnLock() throws Exception{
//        //Details:
//        //start 2 threads and monitor if both threads can acquire in the same time the lock
//        //in this method there is a chance to get value 2 in maxValue
//
//        class DummyInt{
//            private int value = 0;
//
//            public void setValue(int value){
//                this.value = value;
//            }
//
//            public int getValue(){
//                return(this.value);
//            }
//        }
//
//        Application application = new Application(new AppContext());
//
//        DummyInt currentValue = new DummyInt();
//        DummyInt maxValue = new DummyInt();
//        DummyInt dummyValue = new DummyInt();
//
//        application.getState().setStillRunning(true);
//
//        Thread thread1 = new Thread(() -> {
//            while (application.getState().isStillRunning()) {
//                ThreadUtil.sleep(1);
//
//                if (application.getState().isLock()) {
//                    continue;
//                }
//
//                //do a dummy instruction
//                dummyValue.setValue(5);
//
//                //acquire lock
//                application.getState().acquireLock();
//                //increment value
//                currentValue.setValue(currentValue.getValue() + 1);
//
//                //set max
//                if (maxValue.getValue() < currentValue.getValue()){
//                    maxValue.setValue(currentValue.getValue());
//                }
//
//                //reset current value
//                currentValue.setValue(0);
//                application.getState().releaseLock();
//            }
//        });
//        thread1.start();
//
//        Thread thread2 = new Thread(() -> {
//            while (application.getState().isStillRunning()) {
//                ThreadUtil.sleep(1);
//
//                if (application.getState().isLock()) {
//                    continue;
//                }
//
//                //do a dummy instruction
//                dummyValue.setValue(5);
//
//                //--------------------------------------acquire lock
//                application.getState().acquireLock();
//                //increment value
//                currentValue.setValue(currentValue.getValue() + 1);
//
//                //set max
//                if (maxValue.getValue() < currentValue.getValue()){
//                    maxValue.setValue(currentValue.getValue());
//                }
//
//                //reset current value
//                currentValue.setValue(0);
//                //--------------------------------------release lock
//                application.getState().releaseLock();
//            }
//        });
//        thread2.start();
//
//        for (int i = 0; i < 10; i++){
//            if (maxValue.getValue() > 1)
//            {
//                break;
//            }
//
//            ThreadUtil.sleep(1000);
//        }
//
//        application.getState().setStillRunning(false);
//
//        System.out.println(maxValue.getValue());
//
//        thread1.join();
//        thread2.join();
//
//    }
//
    @Test
    public void testConcurrencyTestOnLockWithCheckAndLock() throws Exception{
        //Details:
        //start 2 threads and monitor if both threads can acquire in the same time the lock
        //in this method there is NO chance to get value other than 1 in maxValue

        class DummyInt{
            private int value = 0;

            public void setValue(int value){
                this.value = value;
            }

            public int getValue(){
                return(this.value);
            }
        }

        Application application = new Application(new AppContext());

        DummyInt currentValue = new DummyInt();
        DummyInt maxValue = new DummyInt();
        DummyInt dummyValue = new DummyInt();

        application.getState().setStillRunning(true);

        Thread thread1 = new Thread(() -> {
            while (application.getState().isStillRunning()) {
                ThreadUtil.sleep(1);

                synchronized (appState.lockerSyncPropose) {
                    //lock acquired

                    //do a dummy instruction
                    dummyValue.setValue(5);

                    //increment value
                    currentValue.setValue(currentValue.getValue() + 1);

                    //set max
                    if (maxValue.getValue() < currentValue.getValue()) {
                        maxValue.setValue(currentValue.getValue());
                    }

                    //reset current value
                    currentValue.setValue(0);
                }
            }
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            while (application.getState().isStillRunning()) {
                ThreadUtil.sleep(1);

                synchronized (appState.lockerSyncPropose) {
                    //lock acquired

                    //do a dummy instruction
                    dummyValue.setValue(5);

                    //increment value
                    currentValue.setValue(currentValue.getValue() + 1);

                    //set max
                    if (maxValue.getValue() < currentValue.getValue()) {
                        maxValue.setValue(currentValue.getValue());
                    }

                    //reset current value
                    currentValue.setValue(0);
                }
            }
        });
        thread2.start();

        for (int i = 0; i < 10; i++){
            if (maxValue.getValue() > 1)
            {
                break;
            }

            ThreadUtil.sleep(1000);
        }

        application.getState().setStillRunning(false);

        System.out.println(maxValue.getValue());

        thread1.join();
        thread2.join();

        TestCase.assertTrue(maxValue.getValue() == 1);

    }
}
