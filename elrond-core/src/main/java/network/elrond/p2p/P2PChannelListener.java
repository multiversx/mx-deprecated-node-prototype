package network.elrond.p2p;

import net.tomp2p.peers.PeerAddress;

public interface P2PChannelListener {

    void onReceiveMessage(PeerAddress sender, P2PBroadcastMessage request) throws InterruptedException;
}
