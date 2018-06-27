package network.elrond.consensus;

public enum ConsensusCommandType {
    COMMITMENT_HASH("COMMITMENT_HASH", ConsensusMessageType.COMMAND),
    COMMITMENT("COMMITMENT", ConsensusMessageType.COMMAND),
    PARTIAL_SIGN("PARTIAL_SIGN", ConsensusMessageType.COMMAND),
    SIGN("SIGN", ConsensusMessageType.COMMAND),;

    private final String _name;
    private final ConsensusMessageType _type;

    ConsensusCommandType(String name, ConsensusMessageType type) {
        this._name = name;
        this._type = type;
    }

    public String getName() {
        return _name;
    }

    public ConsensusMessageType getType() {
        return _type;
    }

    public String toString() {
        return _name;
    }
}
