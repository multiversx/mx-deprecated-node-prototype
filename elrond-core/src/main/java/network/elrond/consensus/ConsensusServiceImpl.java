package network.elrond.consensus;

import javafx.util.Pair;
import net.tomp2p.peers.PeerAddress;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.data.BootstrapService;
import network.elrond.data.LocationType;
import network.elrond.p2p.P2PRequestChannel;
import network.elrond.p2p.P2PRequestChannelName;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConsensusServiceImpl implements ConsensusService {
    private static final Logger logger = LogManager.getLogger(ConsensusServiceImpl.class);
    private final int consensusSize = 4;


    @Override
    public List<byte[]> getConsensusNodesForRound(AppState state, long roundIndex) {
        logger.traceEntry("params: {} {}", state, roundIndex);

        logger.info("getting nodes for consensus in round {}", roundIndex);
        Util.check(state != null, "application != null");
        Util.check(roundIndex >= 0, "roundIndex >= 0");

        List<byte[]> signers = new ArrayList<>();

        Block prevRoundBlock = getBlockForRound(state, roundIndex - 1);
        if (prevRoundBlock != null) {
            byte[] signature = prevRoundBlock.getSignature();
            signers = calculateConsensusGroup(state, signature);
        }

        return logger.traceExit(signers);
    }

    private Block getBlockForRound(AppState state, long roundIndex) {
        logger.traceEntry("params: {} {}", state, roundIndex);
        Util.check(state != null, "state != null");
        Util.check(roundIndex >= 0, "roundIndex >= 0");

        Block block = null;
        Blockchain blockchain = state.getBlockchain();
        BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {
            BigInteger blockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.LOCAL, state.getBlockchain());
            if (blockIndex.compareTo(BigInteger.ZERO) < 0) {
                return null;
            }

            String blockHash = bootstrapService.getBlockHashFromIndex(blockIndex, blockchain);
            block = blockchainService.get(blockHash, blockchain, BlockchainUnitType.BLOCK);

            while (roundIndex <= block.getRoundIndex() && blockIndex.compareTo(BigInteger.ONE) >= 0) {
                blockIndex = blockIndex.subtract(BigInteger.ONE);
                blockHash = bootstrapService.getBlockHashFromIndex(blockIndex, blockchain);
                block = blockchainService.get(blockHash, blockchain, BlockchainUnitType.BLOCK);
            }
        } catch (Exception e) {
            logger.catching(e);
        }

        return logger.traceExit(block);
    }

    private List<byte[]> calculateConsensusGroup(AppState state, byte[] signature) {
        logger.traceEntry("params: {} {}", state, signature);
        Util.check(signature != null, "signature != null");
        Util.check(signature.length != 0, "signature.length != 0");

        List<byte[]> publicKeysList = new ArrayList<>();
        List<Pair<PeerAddress, byte[]>> mappingEligibleList = getEligibleList(state);

        if (mappingEligibleList.size() < consensusSize) {
            return logger.traceExit(publicKeysList);
        }

        mappingEligibleList.sort((o1, o2) -> {
            BigInteger o1Int = new BigInteger(o1.getValue());
            BigInteger o2Int = new BigInteger(o2.getValue());
            return o1Int.compareTo(o2Int);
        });

        logger.info("ordered consensus list: ");
        for (Pair pair : mappingEligibleList) {
            logger.info("{}:{}", ((PeerAddress) pair.getKey()).peerId(), new BigInteger((byte[]) pair.getValue()));
        }


        int eligibleListSize = mappingEligibleList.size();
        BigInteger eligibleListSizeBigInt = BigInteger.valueOf(eligibleListSize);

        while (publicKeysList.size() < Math.min(consensusSize, eligibleListSize)) {
            byte[] result = Util.SHA3.get().digest(Util.concatenateArrays(signature, new byte[]{(byte) publicKeysList.size()}));
            int index = new BigInteger(result).mod(eligibleListSizeBigInt).intValue();

            while (publicKeysList.contains(mappingEligibleList.get(index).getValue())) {
                index++;
                index %= eligibleListSize;
            }
            publicKeysList.add(mappingEligibleList.get(index).getValue());
        }

        return logger.traceExit(publicKeysList);
    }


    public List<Pair<PeerAddress, byte[]>> getEligibleList(AppState state) {
        logger.traceEntry("params: {}", state);
        List<Pair<PeerAddress, byte[]>> map = new ArrayList<>();

        if (state == null) {
            logger.warn("appState is null");
            return map;
        }

        Shard currentShard = state.getShard();

        try {
            P2PRequestChannel channel = state.getChanel(P2PRequestChannelName.PUBLIC_KEY);
            map = AppServiceProvider.getP2PRequestService().get(channel, currentShard, P2PRequestChannelName.PUBLIC_KEY, null);
        } catch (Exception ex) {
            logger.throwing(ex);
        }
        return logger.traceExit(map);
    }
}
