package network.elrond.p2p.model;

import java.io.Serializable;

public class P2PBroadcastMessage implements Serializable {

    private final Object payload;
    private final P2PBroadcastChannelName channelName;

    public P2PBroadcastMessage(P2PBroadcastChannelName channelName, Object payload) {
        this.payload = payload;
        this.channelName = channelName;
    }

    public Object getPayload() {
        return payload;
    }

    public P2PBroadcastChannelName getChannelName() {
        return channelName;
    }

    public boolean isForChannel(P2PBroadcastChannelName channelName) {
        return this.channelName.equals(channelName);
    }

    @Override
    public String toString(){
        return String.format("P2PBroadcastMessage{payload=%s, channelName=%s}", payload.toString(), channelName);
    }
}

