package network.elrond.p2p.service;


import net.tomp2p.peers.PeerAddress;
import network.elrond.p2p.model.P2PBroadcastChannel;
import network.elrond.p2p.model.P2PBroadcastChannelName;
import network.elrond.p2p.model.P2PConnection;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public interface P2PBroadcastService {

    P2PBroadcastChannel createChannel(P2PConnection connection, P2PBroadcastChannelName channelName);

    HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel channel);

    HashSet<PeerAddress> getPeersOnChannel(P2PBroadcastChannel globalChannel, Integer destinationShard);

    boolean publishToChannel(P2PBroadcastChannel channel, Serializable obj, Integer destinationShard);

    boolean unsubscribeFromChannel(P2PBroadcastChannel channel);

    boolean leaveNetwork(List<P2PBroadcastChannel> channel);
}
