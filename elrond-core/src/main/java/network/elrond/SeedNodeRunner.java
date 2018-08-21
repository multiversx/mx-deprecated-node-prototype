package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ResponseObject;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.data.BootstrapType;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import network.elrond.sharding.ShardingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SeedNodeRunner {
    private static final Logger logger = LogManager.getLogger(SeedNodeRunner.class);

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdfSource = new SimpleDateFormat(
                "yyyy-MM-dd HH.mm.ss");
        Util.changeLogsPath("logs/" + Util.getHostName() + " - " + sdfSource.format(new Date()));

        String nodeName = "elrond-node-1";
        Integer port = 31201;
        Integer masterPeerPort = 31201;
        String masterPeerIpAddress = "127.0.0.1";

        ShardingService shardingService = AppServiceProvider.getShardingService();

        String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(shardingService.getPrivateKeyForMinting(new Shard(0)).getValue());

        AppContext context = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName, true);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);


        Thread thread = new Thread(() -> {

            do {
                ThreadUtil.sleep(1);

                if (application.getState().getBlockchain().getPool().getTransactions().size() > 1000){
                    continue;
                }

                PublicKey key = application.getState().getPublicKey();
                AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
                //AccountAddress address = AccountAddress.fromHexString(Util.getAddressFromPublicKey(key.getValue()));
                ResponseObject responseObjectTransaction = facade.send(address, BigInteger.TEN, application);
                if (responseObjectTransaction.isSuccess()){
                    ResponseObject sendersBalance = facade.getBalance(AccountAddress.fromBytes(application.getState().getPublicKey().getValue()), application);
                    ResponseObject receiverBalance = facade.getBalance(address, application);
                    //logger.info("Sender balance: {}, receiver balance: {}", sendersBalance.getPayload(), receiverBalance.getPayload());

//                    String hash = AppServiceProvider.getSerializationService().getHashString(responseObjectTransaction.getPayload().toString());
//                    ResponseObject responseObjectReceipt = facade.getReceipt(hash, application);
//                    logger.info(responseObjectReceipt);
                }


            } while (true);

        });
        thread.start();

    }
}
