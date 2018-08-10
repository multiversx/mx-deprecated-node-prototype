package network.elrond.data;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockHeightMessage implements Serializable {
    private BigInteger blockHeight;
    private Integer shardId;

    public BlockHeightMessage(BigInteger blockHeight, Integer shardId) {
        this.blockHeight = blockHeight;
        this.shardId = shardId;
    }

    public BigInteger getBlockHeight() {
        return blockHeight;
    }

    public Integer getShardId() {
        return shardId;
    }
}
