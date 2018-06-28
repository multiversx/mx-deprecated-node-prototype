package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.chronology.ChronologyService;
import network.elrond.core.ThreadUtil;
import network.elrond.data.Block;
import network.elrond.processor.AppTask;
import network.elrond.processor.impl.executor.ChronologyBlockTask;
import network.elrond.processor.impl.initialization.NtpClientInitializerProcessor;
import network.elrond.service.AppServiceProvider;

public class ChronologyBlockTaskTest {

    public void testEventHandlers() throws Exception{
        ChronologyBlockTask chronologyBlockTask = new ChronologyBlockTask();
        Application application = new Application(new AppContext());

        AppTask ntpClientInitializerProcessor = new NtpClientInitializerProcessor();
        ntpClientInitializerProcessor.process(application);

        chronologyBlockTask.process(application);

        //chronologyBlockTask.MAIN_QUEUE.add(new SubRoundEventHandler());

        do{
            ThreadUtil.sleep(1000);

            if (application.getState().getBlockchain() == null){
                application.getState().setBlockchain(new Blockchain(new BlockchainContext()));
                continue;
            }

            if (application.getState().getBlockchain().getGenesisBlock() == null){
                Block blk = new Block();
                blk.setRoundIndex(0);
                blk.setTimestamp(System.currentTimeMillis());

                application.getState().getBlockchain().setGenesisBlock(blk);
            }

        } while(true);
    }


    public void testTimeConsumingBlocksOnEventHandlers() throws Exception{
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();
        ChronologyBlockTask chronologyBlockTask = new ChronologyBlockTask();
        Application application = new Application(new AppContext());

        AppTask ntpClientInitializerProcessor = new NtpClientInitializerProcessor();
        ntpClientInitializerProcessor.process(application);

        if (application.getState().getBlockchain() == null){
            application.getState().setBlockchain(new Blockchain(new BlockchainContext()));
        }

        System.out.println("Preparing...");

        ThreadUtil.sleep(2000);

        if (application.getState().getBlockchain().getGenesisBlock() == null){
            Block blk = new Block();
            blk.setRoundIndex(0);

            long currentTimeStamp = chronologyService.getSynchronizedTime(application.getState().getNtpClient());

            System.out.println(String.format("Genesis @ [%d] %b", currentTimeStamp, application.getState().getNtpClient().isOffline()));

            blk.setTimestamp(currentTimeStamp);

            application.getState().getBlockchain().setGenesisBlock(blk);
        }

        //chronologyBlockTask.MAIN_QUEUE.add(new DelayingEventHandler());

        chronologyBlockTask.process(application);

        //chronologyBlockTask.MAIN_QUEUE.add(new SubRoundEventHandler());

        //test 20 secs
        for (int i = 0; i < 20; i++){
            ThreadUtil.sleep(1000);
        }
    }

}
