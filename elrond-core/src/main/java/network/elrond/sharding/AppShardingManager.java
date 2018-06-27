package network.elrond.sharding;

import net.tomp2p.peers.PeerAddress;
import network.elrond.application.AppState;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.Round;
import network.elrond.consensus.ConsensusService;
import network.elrond.core.CollectionUtil;
import network.elrond.core.Util;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class AppShardingManager {

    private static final Logger logger = LogManager.getLogger(AppShardingManager.class);

    private Boolean leaderNode;

    private Boolean validatorNode;

    private long roundIndex = -1;

    private static AppShardingManager instance = new AppShardingManager();

    public static AppShardingManager instance() {
        return instance;
    }

    public boolean isLeader() {
        return leaderNode;
    }

    public boolean isValidator() {
        return validatorNode;
    }

    public void calculateAndSetRole(AppState state) {
        logger.traceEntry("params: {}", state);
        P2PBroadcastChanel chanel = state.getChanel(P2PChannelName.BLOCK);
        HashSet<PeerAddress> peers = AppServiceProvider.getP2PBroadcastService().getPeersOnChannel(chanel);
        ConsensusService consensusService = AppServiceProvider.getConsensusService();
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();
        long timestamp = chronologyService.getSynchronizedTime();
        Round round = chronologyService.getRoundFromDateTime(timestamp);

        if (roundIndex == round.getIndex()) {
            return;
        }

        roundIndex = round.getIndex();
        logger.info("Calculating consensus members in round {}", round);

        if (CollectionUtil.size(peers) <= 1) {
            leaderNode = true;
            validatorNode = false;
        } else {
            leaderNode = false;
            validatorNode = false;
            byte[] nodePublicKey = state.getPublicKey().getValue();
            List<byte[]> consensusMembers = consensusService.getConsensusNodesForRound(state, round.getIndex());

            for (byte[] member : consensusMembers) {
                logger.info("consensus member {}", Util.byteArrayToHexString(member));
            }

            if (!consensusMembers.isEmpty()) {
                leaderNode = Arrays.equals(consensusMembers.get(0), nodePublicKey);
                boolean inConsensus = false;

                for (byte[] member : consensusMembers) {
                    if (Arrays.equals(member, nodePublicKey)) {
                        inConsensus = true;
                        break;
                    }
                }

                validatorNode = (!leaderNode) && inConsensus;
                logger.info("this node: {}", Util.byteArrayToHexString(nodePublicKey));
            } else {
                logger.error("consensus list is empty");
            }
        }

        logger.traceExit();
    }
}
