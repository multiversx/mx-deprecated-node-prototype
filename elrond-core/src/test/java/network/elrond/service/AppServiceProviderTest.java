package network.elrond.service;

import network.elrond.account.AccountStateServiceImpl;
import network.elrond.blockchain.BlockchainServiceImpl;
import network.elrond.chronology.ChronologyServiceImpl;
import network.elrond.consensus.SPoSServiceImpl;
import network.elrond.consensus.ValidatorServiceImpl;
import network.elrond.crypto.MultiSignatureServiceBNImpl;
import network.elrond.crypto.SignatureServiceSchnorrImpl;
import network.elrond.data.*;
import network.elrond.data.service.BootstrapServiceImpl;
import network.elrond.data.service.ExecutionServiceImpl;
import network.elrond.data.service.SerializationServiceImpl;
import network.elrond.data.service.TransactionService;
import network.elrond.data.service.TransactionServiceImpl;
import network.elrond.p2p.service.P2PBroadcastServiceImpl;

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

//    @Test
//    public void TestDefaultP2PObjectService(){
//        AppServiceProvider.InjectDefaultServices();
//        Assert.assertEquals(P2PObjectServiceImpl.class, AppServiceProvider.getP2PObjectService().getClass());
//    }

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
        Assert.assertEquals(SignatureServiceSchnorrImpl.class, AppServiceProvider.getSignatureService().getClass());
    }

    @Test
    public void TestDefaultMultiSignatureService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(MultiSignatureServiceBNImpl.class, AppServiceProvider.getMultiSignatureService().getClass());
    }

    @Test
    public void TestDefaultAccountStateService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(AccountStateServiceImpl.class, AppServiceProvider.getAccountStateService().getClass());
    }

    @Test
    public void TestDefaultTransactionExecutionService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(ExecutionServiceImpl.class, AppServiceProvider.getExecutionService().getClass());
    }

//    @Test
//    public void TestDefaultAppPersistenceService (){
//        AppServiceProvider.InjectDefaultServices();
//        Assert.assertEquals(AppPersistenceServiceImpl.class, AppServiceProvider.getAppPersistanceService().getClass());
//    }

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

    @Test
    public void TestDefaultChronologyService (){
        AppServiceProvider.InjectDefaultServices();
        Assert.assertEquals(ChronologyServiceImpl.class, AppServiceProvider.getChronologyService().getClass());
    }
}
