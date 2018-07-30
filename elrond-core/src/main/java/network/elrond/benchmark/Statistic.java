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

//    public void setNrTransactionsInBlock(long nrTransactionsInBlock) {
//        this.nrTransactionsInBlock = nrTransactionsInBlock;
//    }
//
//    public long getTps() {
//        return tps;
//    }
//
//    public void setTps(long tps) {
//        this.tps = tps;
//    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public long getCurrentBlockNonce() {
        return currentBlockNonce;
    }
//
//    public void setCurrentTimeMillis(long currentTimeMillis) {
//        this.currentTimeMillis = currentTimeMillis;
//    }
}