package network.elrond.p2p;

import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.p2p.handlers.*;

public enum P2PRequestChannelName {

    ACCOUNT("ACCOUNT", new AccountRequestHandler()),
    BLOCK(BlockchainUnitType.BLOCK.name(), new BlockRequestHandler()),
    TRANSACTION(BlockchainUnitType.TRANSACTION.name(), new TransactionRequestHandler()),
    STATISTICS("STATISTICS", new StatisticsRequestHandler()),;

    private final String name;
    private final RequestHandler handler;

    P2PRequestChannelName(final String name, final RequestHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public RequestHandler getHandler() {
        return handler;
    }

    public static P2PRequestChannelName getFromName(String name) {
        for (P2PRequestChannelName channel : P2PRequestChannelName.values()) {
            if (name.equals(channel.getName())) {
                return channel;
            }
        }
        return null;
    }
}
