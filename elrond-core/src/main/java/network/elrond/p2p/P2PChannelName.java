package network.elrond.p2p;

public enum P2PChannelName {

    BLOCK("BLOCK", P2PChannelType.SHARD_LEVEL),
    TRANSACTION("TRANSACTION", P2PChannelType.SHARD_LEVEL),

    RECEIPT("RECEIPT", P2PChannelType.SHARD_LEVEL),
    TRANSACTION_RECEIPT("TRANSACTION_RECEIPT", P2PChannelType.SHARD_LEVEL),

    XTRANSACTION("XTRANSACTION", P2PChannelType.GLOBAL_LEVEL),
    XRECEIPT("XRECEIPT", P2PChannelType.GLOBAL_LEVEL),

    CONSENSUS("CONSENSUS", P2PChannelType.SHARD_LEVEL);

    private final String _name;
    private final P2PChannelType _type;

    P2PChannelName(String name, P2PChannelType type) {
        this._name = name;
        this._type = type;
    }

    public P2PChannelType getType() {
        return _type;
    }


    public String toString() {
        return _name;
    }


}
