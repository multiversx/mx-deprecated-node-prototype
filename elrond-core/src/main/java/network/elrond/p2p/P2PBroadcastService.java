package network.elrond.p2p;


import network.elrond.application.AppContext;

import java.io.IOException;
import java.util.List;

public interface P2PBroadcastService {

    P2PConnection createConnection(AppContext context) throws IOException;

    P2PConnection createConnection(
            Integer peerId,
            int peerPort,
            String masterPeerIpAddress,
            int masterPeerPort
    ) throws IOException;


    P2PBroadcastChanel createChannel(P2PConnection connection, String chanelName);

    boolean subscribeToChannel(P2PBroadcastChanel chanel);

    boolean publishToChannel(P2PBroadcastChanel chanel, Object obj);

    boolean unsubscribeFromChannel(P2PBroadcastChanel chanel);

    boolean leaveNetwork(List<P2PBroadcastChanel> chanel);

}
