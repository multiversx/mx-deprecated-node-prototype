package network.elrond.consensus;

import network.elrond.chronology.Round;
import network.elrond.core.CollectionUtil;
import network.elrond.core.Util;

import java.util.List;

public class ConsensusServiceImpl implements ConsensusService {


    @Override
    public String computeLeader(List<String> nodes, Round round) {
        Util.check(round != null, "round != null");
        Util.check(!CollectionUtil.isEmpty(nodes), "nodes empty");

        int index = (int) (round.getIndex() % (long) nodes.size());
        return nodes.get(index);

    }
}
