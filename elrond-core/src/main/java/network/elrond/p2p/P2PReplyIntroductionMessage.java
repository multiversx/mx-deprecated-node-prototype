package network.elrond.p2p;

import net.tomp2p.peers.PeerAddress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class P2PReplyIntroductionMessage {
    private List<P2PIntroductionMessage> bucketList = new ArrayList<>();

    public P2PReplyIntroductionMessage(Map<Integer, HashSet<PeerAddress>> bucketPeers) {
        for (Integer shardId : bucketPeers.keySet()) {
            for (PeerAddress peerAddress : bucketPeers.get(shardId)) {
                bucketList.add(new P2PIntroductionMessage(peerAddress, shardId));
            }
        }
    }

    public List<P2PIntroductionMessage> getBucketList() {
        return bucketList;
    }
}
