package network.elrond.benchmark;

public class Statistic {
    private long nrTransactionsInBlock;
    private long tps;

    public long getNrTransactionsInBlock() {
        return nrTransactionsInBlock;
    }

    public void setNrTransactionsInBlock(long nrTransactionsInBlock) {
        this.nrTransactionsInBlock = nrTransactionsInBlock;
    }

    public long getTps() {
        return tps;
    }

    public void setTps(long tps) {
        this.tps = tps;
    }
}
