package network.elrond.p2p;


import net.tomp2p.dht.FuturePut;
import network.elrond.application.AppContext;

import java.io.IOException;
import java.util.List;

public interface P2PBroadcastService {

    P2PBroadcastConnection createConnection(AppContext context) throws IOException;

    P2PBroadcastChanel createChannel(P2PBroadcastConnection connection, String chanelName);

    boolean subscribeToChannel(P2PBroadcastChanel chanel);

    boolean publishToChannel(P2PBroadcastChanel chanel, Object obj);

    boolean unsubscribeFromChannel(P2PBroadcastChanel chanel);

    boolean leaveNetwork(List<P2PBroadcastChanel> chanel);

    Object get(P2PBroadcastChanel chanel, String key) throws ClassNotFoundException, IOException;

    FuturePut put(P2PBroadcastChanel chanel, String key, Object value) throws IOException;
}
