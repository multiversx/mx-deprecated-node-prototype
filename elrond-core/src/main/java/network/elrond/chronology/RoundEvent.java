package network.elrond.chronology;

public enum RoundEvent {
    COMPUTE_CONSENSUS_END("COMPUTE_CONSENSUS_END"),
    PROPOSE_BLOCK_END("PROPOSE_BLOCK_END"),
    COMMITMENT_HASH_END("COMMITMENT_HASH_END"),
    COMMITMENT_END("COMMITMENT_END"),
    PARTIAL_SIGN_END("PARTIAL_SIGN_END"),;

    private final String _name;

    RoundEvent(String name) {
        this._name = name;
    }

    public String getName() {
        return _name;
    }

    public String toString() {
        return _name;
    }
}
