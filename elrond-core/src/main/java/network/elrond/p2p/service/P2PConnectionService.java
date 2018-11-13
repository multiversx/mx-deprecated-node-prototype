package network.elrond.p2p.service;


import network.elrond.application.AppContext;
import network.elrond.p2p.model.P2PConnection;

import java.io.IOException;
import java.io.Serializable;

public interface P2PConnectionService {

    P2PConnection createConnection(AppContext context) throws IOException;

    P2PConnection createConnection(
            String nodeName,
            int peerPort,
            String masterPeerIpAddress,
            int masterPeerPort
    ) throws IOException;

    <T extends Serializable> void broadcastMessage(T object, P2PConnection connection);

}
