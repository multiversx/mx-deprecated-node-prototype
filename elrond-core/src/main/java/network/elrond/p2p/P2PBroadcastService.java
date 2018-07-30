package network.elrond.p2p;


import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public interface P2PBroadcastService {

    P2PBroadcastChannel createChannel(P2PConnection connection, P2PBroadcastChannelName chanelName);

    boolean subscribeToChannel(P2PBroadcastChannel chanel);

    HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel channel);

    HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel globalChannel, Integer destinationShard);

    boolean publishToChannel(P2PBroadcastChannel chanel, Serializable obj, Integer destinationShard);

    boolean unsubscribeFromChannel(P2PBroadcastChannel chanel);

    boolean leaveNetwork(List<P2PBroadcastChannel> chanel);

}
