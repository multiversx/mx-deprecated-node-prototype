package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.benchmark.Statistic;
import network.elrond.chronology.SubRound;
import network.elrond.core.EventHandler;

public class EndRoundHandler implements EventHandler<SubRound> {
    @Override
    public void onEvent(AppState state, SubRound data) {
        state.getStatisticsManager().updateNetworkStats(state);

        boolean isLastBlockFromCurentRound = (state.getBlockchain().getCurrentBlock() != null) &&
                (state.getBlockchain().getCurrentBlock().getRoundIndex() == data.getRound().getIndex());

        if (!isLastBlockFromCurentRound){
            if (state.getStatisticsManager() != null) {
                state.getStatisticsManager().addStatistic(new Statistic(0));
            }
        }
    }
}
