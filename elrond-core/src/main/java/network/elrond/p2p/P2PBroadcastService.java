package network.elrond.p2p;


import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public interface P2PBroadcastService {

    P2PBroadcastChanel createChannel(P2PConnection connection, P2PBroadcastChannelName chanelName);

    boolean subscribeToChannel(P2PBroadcastChanel chanel);

    HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChanel channel);

    HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChanel globalChannel, Integer destinationShard);

    boolean publishToChannel(P2PBroadcastChanel chanel, Serializable obj, Integer destinationShard);

    boolean unsubscribeFromChannel(P2PBroadcastChanel chanel);

    boolean leaveNetwork(List<P2PBroadcastChanel> chanel);

}
