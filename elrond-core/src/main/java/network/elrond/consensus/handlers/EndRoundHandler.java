package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.chronology.SubRound;
import network.elrond.core.EventHandler;

public class EndRoundHandler implements EventHandler<SubRound> {
    @Override
    public void onEvent(AppState state, SubRound data) {
        state.getStatisticsManager().updateNetworkStats(state);
        state.getStatisticsManager().processStatistic();

//        if (state.getStatisticsManager() != null) {
//            boolean isLastBlockFromCurentRound = (state.getBlockchain().getCurrentBlock() != null) &&
//                (state.getBlockchain().getCurrentBlock().getRoundIndex() == data.getRound().getIndex());
//
//            if (!isLastBlockFromCurentRound){
//                state.getStatisticsManager().addStatistic(new Statistic(0));
//            } else {
//                state.getStatisticsManager().addStatistic(new Statistic(state.getBlockchain().getCurrentBlock().getListTXHashes().size()));
//            }
//        }
    }
}
