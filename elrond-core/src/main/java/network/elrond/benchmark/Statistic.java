package network.elrond.benchmark;

import java.io.Serializable;

public class Statistic implements Serializable {
    private long nrTransactionsInBlock;
    private long currentBlockNonce;
    private long currentTimeMillis;

    public Statistic(long nrTransactionsInBlock, long blockNonce) {
        this(nrTransactionsInBlock);
        this.currentBlockNonce = blockNonce;
    }

    public Statistic(long nrTransactionsInBlock) {
        this.nrTransactionsInBlock = nrTransactionsInBlock;
        currentTimeMillis = System.currentTimeMillis();
    }

    public long getNrTransactionsInBlock() {
        return nrTransactionsInBlock;
    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public long getCurrentBlockNonce() {
        return currentBlockNonce;
    }

}