package network.elrond.p2p.model;

import network.elrond.sharding.Shard;

import java.io.Serializable;

public class P2PRequestMessage implements Serializable {

    private final Object key;
    private final Shard requester;
    private final P2PRequestChannelName channelName;

    public P2PRequestMessage(Object key, P2PRequestChannelName channelName, Shard requester) {
        this.key = key;
        this.requester = requester;
        this.channelName = channelName;
    }

    public Object getKey() {
        return key;
    }

    public Shard getRequester() {
        return requester;
    }

    public P2PRequestChannelName getChannelName() {
        return channelName;
    }

}

