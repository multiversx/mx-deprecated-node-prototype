package network.elrond.p2p;

import java.io.Serializable;

public class P2PBroadcastMessage implements Serializable {

    private Object payload;
    private P2PChannelName channelName;

    public P2PBroadcastMessage(P2PChannelName channelName, Object payload) {
        this.payload = payload;
        this.channelName = channelName;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public P2PChannelName getChannelName() {
        return channelName;
    }

    public void setChannelName(P2PChannelName channelName) {
        this.channelName = channelName;
    }

    public boolean isForChannel(P2PChannelName channelName) {
        return this.channelName.equals(channelName);
    }
}

