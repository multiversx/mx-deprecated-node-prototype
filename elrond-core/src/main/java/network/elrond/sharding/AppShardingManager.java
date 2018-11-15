package network.elrond.sharding;

import net.tomp2p.peers.PeerAddress;
import network.elrond.application.AppState;
import network.elrond.p2p.model.P2PBroadcastChannel;
import network.elrond.p2p.model.P2PBroadcastChannelName;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AppShardingManager {

    private Boolean isSeedNode;

    private static AppShardingManager instance = new AppShardingManager();

    public static AppShardingManager instance() {
        return instance;
    }

    public boolean isLeaderInShard(AppState state) {

        if (isSeedNode == null) {
            P2PBroadcastChannel chanel = state.getChannel(P2PBroadcastChannelName.BLOCK);
            HashSet<PeerAddress> peers = AppServiceProvider.getP2PBroadcastService().getPeersOnChannel(chanel);
            List<PeerAddress> listPeers = new ArrayList<>(peers);

            isSeedNode = listPeers.get(0).equals(state.getConnection().getPeer().peerAddress());
        }

        return isSeedNode;
    }

    public Integer getNumberNodesInShard(AppState state) {

        P2PBroadcastChannel channel = state.getChannel(P2PBroadcastChannelName.BLOCK);
        Integer nbPeers = getConnectedPeersOnChannel(channel).size();

        return nbPeers;
    }

    public Integer getNumberNodesInNetwork(AppState state) {
        P2PBroadcastChannel channel = state.getChannel(P2PBroadcastChannelName.XTRANSACTION_BLOCK);
        Integer nbPeers = getConnectedPeersOnChannel(channel).size();

        return nbPeers;
    }


    public List<String> getConnectedPeersOnChannel(P2PBroadcastChannel channel) {
        // get only alive nodes
        return AppServiceProvider.getP2PBroadcastService().getPeersOnChannel(channel)
                .stream()
                .filter(Objects::nonNull)
                .map(peerAddress -> peerAddress.peerId().toString())
                .sorted()
                .collect(Collectors.toList());
    }


    public List<String> getPeersOnShardInBlock(AppState state) {
        HashSet<String> nodeList = new HashSet<String>();

        if (state.getBlockchain() != null && state.getBlockchain().getCurrentBlock() != null) {
            nodeList.addAll(state.getBlockchain().getCurrentBlock().getPeers());
        }

        HashSet<PeerAddress> totalPeers = state.getConnection().getPeersOnShard(state.getShard().getIndex());

        for (PeerAddress peer : totalPeers) {
            nodeList.add(peer.peerId().toString());
        }

        String self = state.getConnection().getPeer().peerID().toString();

        if (!nodeList.contains(self)) {
            nodeList.add(self);
        }

        return nodeList.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    public String getCurrentPeerID(AppState state) {
        return state.getConnection().getPeer().peerID().toString();
    }
}
