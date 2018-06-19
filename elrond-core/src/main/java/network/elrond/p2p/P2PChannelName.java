package network.elrond.p2p;

public enum P2PChannelName {

    BLOCK("BLOCK"),
    TRANSACTION("TRANSACTION"),
    TRANSACTION_RECEIPT("TRANSACTION_RECEIPT"),
    RECEIPT("RECEIPT"),
    ;

    private final String _name;

    P2PChannelName(String name) {
        this._name = name;
    }

    public String toString() {
        return _name;
    }
}
