package network.elrond.p2p;

import net.tomp2p.peers.PeerAddress;

public interface P2PChannelListener {

    void onReciveMessage(PeerAddress sender, P2PBroadcastMessage request) throws InterruptedException;
}
