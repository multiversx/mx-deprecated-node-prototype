package network.elrond.service;

import network.elrond.account.AccountStateServiceImpl;
import network.elrond.blockchain.BlockchainServiceImpl;
import network.elrond.consensus.SPoSServiceImpl;
import network.elrond.consensus.ValidatorServiceImpl;
import network.elrond.crypto.BNMultiSignatureServiceImpl;
import network.elrond.crypto.SchnorrSignatureServiceImpl;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastServiceImpl;
import network.elrond.p2p.P2PObjectServiceImpl;
import org.junit.Assert;
import org.junit.Test;

public class AppServiceProviderTest {

    @Test
    public void TestDefaultP2PBroadcastService(){
        Assert.assertEquals(P2PBroadcastServiceImpl.class, AppServiceProvider.getP2PBroadcastService().getClass());
    }

    @Test
    public void TestDefaultSerializationService(){
        Assert.assertEquals(SerializationServiceImpl.class, AppServiceProvider.getSerializationService().getClass());
    }

    @Test
    public void TestDefaultP2PObjectService(){
        Assert.assertEquals(P2PObjectServiceImpl.class, AppServiceProvider.getP2PObjectService().getClass());
    }

    @Test
    public void TestDefaultTransactionService (){
        Assert.assertEquals(TransactionServiceImpl.class, AppServiceProvider.getTransactionService().getClass());
    }

    @Test
    public void TestDefaultBlockService (){
        Assert.assertEquals(BlockServiceImpl.class, AppServiceProvider.getBlockService().getClass());
    }

    @Test
    public void TestDefaultValidatorService (){
        Assert.assertEquals(ValidatorServiceImpl.class, AppServiceProvider.getValidatorService().getClass());
    }

    @Test
    public void TestDefaultSPoSService (){
        Assert.assertEquals(SPoSServiceImpl.class, AppServiceProvider.getSPoSService().getClass());
    }

    @Test
    public void TestDefaultBlockchainService (){
        Assert.assertEquals(BlockchainServiceImpl.class, AppServiceProvider.getBlockchainService().getClass());
    }

    @Test
    public void TestDefaultSignatureService (){
        Assert.assertEquals(SchnorrSignatureServiceImpl.class, AppServiceProvider.getSignatureService().getClass());
    }

    @Test
    public void TestDefaultMultiSignatureService (){
        Assert.assertEquals(BNMultiSignatureServiceImpl.class, AppServiceProvider.getMultiSignatureService().getClass());
    }

    @Test
    public void TestDefaultAccountStateService (){
        Assert.assertEquals(AccountStateServiceImpl.class, AppServiceProvider.getAccountStateService().getClass());
    }

    @Test
    public void TestDefaultTransactionExecutionService (){
        Assert.assertEquals(TransactionExecutionServiceImpl.class, AppServiceProvider.getTransactionExecutionService().getClass());
    }

    @Test(expected = NullPointerException.class)
    public void TestPutNullService(){
       AppServiceProvider.putService(TransactionService.class, null);
    }
}
