package network.elrond.p2p;

import network.elrond.p2p.handlers.*;

public enum P2PRequestChannelName {

    ACCOUNT("ACCOUNT", new AccountRequestHandler()),
    BLOCK("BLOCK", new BlockRequestHandler()),
    TRANSACTION("TRANSACTION", new TransactionRequestHandler()),
    RECEIPT("RECEIPT", new ReceiptRequestHandler()),
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
}

