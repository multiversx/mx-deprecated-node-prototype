package network.elrond.p2p.model;

import network.elrond.sharding.Shard;

import java.io.Serializable;

public class P2PRequestMessage implements Serializable {

    private Object key;
    private Shard requester;
    private P2PRequestChannelName channelName;

    public P2PRequestMessage(Object key, P2PRequestChannelName channelName, Shard requester) {
        this.key = key;
        this.requester = requester;
        this.channelName = channelName;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Shard getRequester() {
        return requester;
    }

    public void setRequester(Shard requester) {
        this.requester = requester;
    }

    public P2PRequestChannelName getChannelName() {
        return channelName;
    }

    public void setChannelName(P2PRequestChannelName channelName) {
        this.channelName = channelName;
    }
}

