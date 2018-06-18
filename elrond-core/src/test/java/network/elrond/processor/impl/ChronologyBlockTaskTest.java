package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.SubRoundEventHandler;
import network.elrond.chronology.RoundState;
import network.elrond.core.ThreadUtil;
import network.elrond.data.Block;
import network.elrond.processor.AppTask;
import network.elrond.service.AppServiceProvider;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ChronologyBlockTaskTest {

    @Ignore
    @Test
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
                blk.setRoundHeight(0);
                blk.setTimestamp(System.currentTimeMillis());

                application.getState().getBlockchain().setGenesisBlock(blk);
            }

        } while(true);


    }

    @Test
    public void testGetCurrentSubRoundType(){
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();

        ChronologyBlockTask chronologyBlockTask = new ChronologyBlockTask();

        List<RoundState> listFound = new ArrayList<>();

        for (long i = 0; i < chronologyService.getRoundTimeDuration(); i++){
            RoundState subRoundsType = chronologyBlockTask.computeRoundState(0, i);

            if (!listFound.contains(subRoundsType)){
                System.out.println(String.format("Found %s @ %d", subRoundsType, i));
                listFound.add(subRoundsType);
            }
        }
    }

}
