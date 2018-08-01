package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;

public class EndRoundHandler extends EventHandler {

    public EndRoundHandler(long currentRoundIndex){
        super(currentRoundIndex);
    }

    @Override
    public EventHandler execute(AppState state, long genesisTimeStamp) {
        while (isStillInRound(state, genesisTimeStamp)){
            ThreadUtil.sleep(10);
        }

        return new StartRoundHandler(0);
    }
}
