package network.elrond.service;

import network.elrond.account.AccountStateServiceImpl;
import network.elrond.blockchain.AppPersistenceServiceImpl;
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
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(P2PBroadcastServiceImpl.class, AppServiceProvider.getP2PBroadcastService().getClass());
    }

    @Test
    public void TestDefaultSerializationService(){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(SerializationServiceImpl.class, AppServiceProvider.getSerializationService().getClass());
    }

    @Test
    public void TestDefaultP2PObjectService(){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(P2PObjectServiceImpl.class, AppServiceProvider.getP2PObjectService().getClass());
    }

    @Test
    public void TestDefaultTransactionService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(TransactionServiceImpl.class, AppServiceProvider.getTransactionService().getClass());
    }

    @Test
    public void TestDefaultValidatorService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(ValidatorServiceImpl.class, AppServiceProvider.getValidatorService().getClass());
    }

    @Test
    public void TestDefaultSPoSService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(SPoSServiceImpl.class, AppServiceProvider.getSPoSService().getClass());
    }

    @Test
    public void TestDefaultBlockchainService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(BlockchainServiceImpl.class, AppServiceProvider.getBlockchainService().getClass());
    }

    @Test
    public void TestDefaultSignatureService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(SchnorrSignatureServiceImpl.class, AppServiceProvider.getSignatureService().getClass());
    }

    @Test
    public void TestDefaultMultiSignatureService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(BNMultiSignatureServiceImpl.class, AppServiceProvider.getMultiSignatureService().getClass());
    }

    @Test
    public void TestDefaultAccountStateService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(AccountStateServiceImpl.class, AppServiceProvider.getAccountStateService().getClass());
    }

    @Test
    public void TestDefaultTransactionExecutionService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(TransactionExecutionServiceImpl.class, AppServiceProvider.getTransactionExecutionService().getClass());
    }

    @Test
    public void TestDefaultAppPersistenceService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(AppPersistenceServiceImpl.class, AppServiceProvider.getAppPersistanceService().getClass());
    }

    @Test
    public void TestDefaultBootstrapService(){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(BootstrapServiceImpl.class, AppServiceProvider.getBootstrapService().getClass());
    }

    @Test(expected = NullPointerException.class)
    public void TestPutNullServiceShouldThrowException(){
        AppServiceProvider.InjectDefaultServices();
        AppServiceProvider.putService(TransactionService.class, null);
    }
}
