package network.elrond.data;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.BlockchainPersistenceUnit;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.util.Scanner;

public class BootstrapTest {
    @Test
    public void bootstrapMethodsTest(){
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001 /*+ new Random().nextInt(10000)*/);
        context.setPeerId(0);
        context.setBootstrapType(BootstrapType.REBUILD_FROM_DISK);
        context.setStorageBasePath("test");

        Application app = new Application(context);
        AppState state = app.getState();
        state.setStillRunning(false);

        try {
            app.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //delete stored data
        for (BlockchainUnitType blockchainUnitType : BlockchainUnitType.values()) {
            BlockchainPersistenceUnit<Object, Object> blockchainPersistenceUnit = state.getBlockchain().getUnit(blockchainUnitType);

            try {
                blockchainPersistenceUnit.destroyAndReCreate();
            } catch (Exception ex) {

            }




            // do what you want
        }

        //test 1: test whether




        app.stop();
    }

}
