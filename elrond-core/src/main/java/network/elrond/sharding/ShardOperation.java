package network.elrond.sharding;

public enum ShardOperation {

    INTER_SHARD_OUT(true, false),
    INTER_SHARD_IN(false, true),
    INTRA_SHARD(true, true),;


    boolean checkSource;
    boolean checkTarget;

    ShardOperation(boolean checkSource, boolean checkTarget) {
        this.checkSource = checkSource;
        this.checkTarget = checkTarget;
    }

    public boolean isCheckSource() {
        return checkSource;
    }

    public boolean isCheckTarget() {
        return checkTarget;
    }
}
