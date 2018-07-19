package network.elrond.p2p;

public enum P2PBroadcastChannelName {

    BLOCK("BLOCK", P2PChannelType.SHARD_LEVEL),
    TRANSACTION("TRANSACTION", P2PChannelType.SHARD_LEVEL),

    //RECEIPT("RECEIPT", P2PChannelType.SHARD_LEVEL),
    TRANSACTION_RECEIPT("TRANSACTION_RECEIPT", P2PChannelType.SHARD_LEVEL),
    RECEIPT_BLOCK("RECEIPT_BLOCK", P2PChannelType.SHARD_LEVEL),

    XRECEIPT_BLOCK("XRECEIPT_BLOCK", P2PChannelType.GLOBAL_LEVEL),
    XTRANSACTION_BLOCK("XTRANSACTION_BLOCK", P2PChannelType.GLOBAL_LEVEL),
    //XTRANSACTION("XTRANSACTION", P2PChannelType.GLOBAL_LEVEL),
    XRECEIPT("XRECEIPT", P2PChannelType.GLOBAL_LEVEL),;

    private final String _name;
    private final P2PChannelType _type;

    P2PBroadcastChannelName(String name, P2PChannelType type) {
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
