package network.elrond.p2p;

import net.tomp2p.message.Message;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.StructuredBroadcastHandler;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class P2PBroadcastMessageHandler extends StructuredBroadcastHandler {
    private static final Logger logger = LogManager.getLogger(P2PConnectionServiceImpl.class);
    Peer peer;

    @Override
    public StructuredBroadcastHandler receive(Message message) {
        try {
            P2PIntroductionMessage introductionMessage = (P2PIntroductionMessage) message.dataMap(0).dataMap().get(Number640.ZERO).object();
            logger.debug("{} received broadcast message from: {}", peer.peerID(), introductionMessage.getPeerId());
            PeerAddress peerAddress = new PeerAddress(introductionMessage.getPeerId(),
                    introductionMessage.getInetAddress(),
                    introductionMessage.getTcpPort(),
                    introductionMessage.getUdpPort());



        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return super.receive(message);
    }

    @Override
    public StructuredBroadcastHandler init(Peer peer) {
        this.peer = peer;
        return super.init(peer);
    }
}
