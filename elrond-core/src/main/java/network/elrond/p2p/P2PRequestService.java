package network.elrond.p2p;

import network.elrond.sharding.Shard;

import java.io.Serializable;

public interface P2PRequestService {

    P2PRequestChannel createChannel(P2PConnection connection, Shard shard, P2PRequestChannelName channelName);

    <K extends Serializable, R extends Serializable> R get(P2PRequestChannel channel, Shard shard, P2PRequestChannelName channelName, K key);
}
