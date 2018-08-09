package network.elrond.p2p;


import network.elrond.application.AppContext;
import network.elrond.sharding.Shard;

import java.io.IOException;

public interface P2PConnectionService {

    P2PConnection createConnection(AppContext context) throws IOException;

    P2PConnection createConnection(
            String nodeName,
            int peerPort,
            String masterPeerIpAddress,
            int masterPeerPort
    ) throws IOException;

    void introduceSelf(Shard shard, P2PConnection connection);

}
