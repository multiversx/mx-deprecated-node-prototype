package network.elrond.core;

import network.elrond.application.AppState;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.Round;
import network.elrond.service.AppServiceProvider;

//public interface EventHandler<D, Q> {
public abstract class EventHandler {

    protected long currentRoundIndex = 0;

    public EventHandler(long currentRoundIndex){
        this.currentRoundIndex = currentRoundIndex;
    }

    public abstract EventHandler execute(AppState state, long genesisTimeStamp);

    public long getCurrentRoundIndex(){
        return currentRoundIndex;
    }

    protected boolean isStillInRound(AppState state, long genesisTimeStamp){
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();
        long currentTimeStamp = chronologyService.getSynchronizedTime(state.getNtpClient());

        Round currentRound = AppServiceProvider.getChronologyService().getRoundFromDateTime(genesisTimeStamp, currentTimeStamp);

        return currentRound.getIndex() == currentRoundIndex;
    }
}
