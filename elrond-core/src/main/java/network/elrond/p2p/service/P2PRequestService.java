package network.elrond.p2p.service;

import network.elrond.p2p.model.P2PConnection;
import network.elrond.p2p.model.P2PRequestChannel;
import network.elrond.p2p.model.P2PRequestChannelName;
import network.elrond.sharding.Shard;

import java.io.Serializable;

public interface P2PRequestService {

    P2PRequestChannel createChannel(P2PConnection connection, Shard shard, P2PRequestChannelName channelName);

    <K extends Serializable, R extends Serializable> R get(P2PRequestChannel channel, Shard shard, P2PRequestChannelName channelName, K key);
}
