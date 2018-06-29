package network.elrond.processor.impl.initialization;

import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.application.AppState;
import network.elrond.benchmark.StatisticsManager;
import network.elrond.p2p.*;
import network.elrond.processor.AppTask;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class P2PRequestObjectStarterProcessor implements AppTask {
    private static final Logger logger = LogManager.getLogger(AbstractChannelTask.class);

    @Override
    public void process(Application application) {

        AppState state = application.getState();
        Shard shard = state.getShard();

        P2PConnection connection = state.getConnection();

        P2PRequestChannel channelAccounts = AppServiceProvider.getP2PRequestService().createChannel(connection, shard, P2PRequestChannelName.ACCOUNT);
        channelAccounts.setHandler((P2PRequestObjectHandler<AccountState>) request -> {
            AccountAddress address = (AccountAddress) request.getKey();
            try {
                AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(address, state.getAccounts());
                return accountState;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        state.addChanel(channelAccounts);

        P2PRequestChannel channelStatistics = AppServiceProvider.getP2PRequestService().createChannel(connection, shard, P2PRequestChannelName.STATISTICS);
        channelStatistics.setHandler((P2PRequestObjectHandler<StatisticsManager>) request -> {
            StatisticsManager statisticsManager = state.getStatisticsManager();
            return statisticsManager;
        });

        state.addChanel(channelStatistics);
    }
}
