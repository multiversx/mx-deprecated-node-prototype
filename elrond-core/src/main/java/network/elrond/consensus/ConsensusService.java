package network.elrond.consensus;

import network.elrond.chronology.Round;

import java.util.List;

public interface ConsensusService {
    String computeLeader(List<String> nodes, Round round);
}
