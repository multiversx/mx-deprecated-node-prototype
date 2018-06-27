package network.elrond.benchmark;

public class Statistic {
    private long nrTransactionsInBlock;
    private long tps;
    private long currentTimeMillis;

    public Statistic(long nrTransactionsInBlock){
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
//
//    public void setCurrentTimeMillis(long currentTimeMillis) {
//        this.currentTimeMillis = currentTimeMillis;
//    }
}
