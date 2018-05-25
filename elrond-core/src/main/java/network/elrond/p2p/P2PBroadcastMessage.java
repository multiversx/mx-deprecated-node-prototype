package network.elrond.p2p;

import java.io.Serializable;

public class P2PBroadcastMessage implements Serializable {

    private Object payload;
    private String channelName;

    public P2PBroadcastMessage(String channelName, Object payload) {
        this.payload = payload;
        this.channelName = channelName;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isForChannel(String channelName) {
        return this.channelName.equals(channelName);
    }
}

