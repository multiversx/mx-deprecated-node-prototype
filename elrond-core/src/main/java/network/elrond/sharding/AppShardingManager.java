package network.elrond.sharding;

import net.tomp2p.peers.PeerAddress;
import network.elrond.application.AppState;
import network.elrond.core.CollectionUtil;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AppShardingManager {

    private static final Logger logger = LogManager.getLogger(AppShardingManager.class);

    private Boolean isSeedNode;

    private static AppShardingManager instance = new AppShardingManager();

    public static AppShardingManager instance() {
        return instance;
    }

    public boolean isLeaderInShard(AppState state) {

        if (isSeedNode == null) {
            P2PBroadcastChanel chanel = state.getChanel(P2PBroadcastChannelName.BLOCK);
            HashSet<PeerAddress> peers = AppServiceProvider.getP2PBroadcastService().getPeersOnChannel(chanel);
            isSeedNode = CollectionUtil.size(peers) <= 1;
        }

        return isSeedNode;
    }

    public List<String> getPeersOnShard(AppState state){
        P2PBroadcastChanel chanel = state.getChanel(P2PBroadcastChannelName.BLOCK);
        return AppServiceProvider.getP2PBroadcastService().getPeersOnChannel(chanel)
                .stream()
                .filter(Objects::nonNull)
                .map(peerAddress -> peerAddress.peerId().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    public String getCurrentPeerID(AppState state){
        return state.getConnection().getPeer().peerID().toString();
    }
}
