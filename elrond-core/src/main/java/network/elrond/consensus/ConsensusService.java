package network.elrond.consensus;

import network.elrond.application.AppState;

import java.util.List;

public interface ConsensusService {
    List<byte[]> getConsensusNodesForRound(AppState state, long roundIndex);
}

