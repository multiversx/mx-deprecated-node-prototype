package network.elrond.benchmark;

public class BenchmarkResult {
    private long activeNodes;
    private long nrShards;
    private long averageRoundTime;
    private long averageNrTxPerBlock;

    private long liveTps;
    private long peakTps;
    private long averageTps;
    private long liveNrTransactionsPerBlock;

    public long getLiveTps() {
        return liveTps;
    }

    public void setLiveTps(long liveTps) {
        this.liveTps = liveTps;
    }

    public long getPeakTps() {
        return peakTps;
    }

    public void setPeakTps(long peakTps) {
        this.peakTps = peakTps;
    }

    public long getAverageTps() {
        return averageTps;
    }

    public void setAverageTps(long averageTps) {
        this.averageTps = averageTps;
    }

    public long getActiveNodes() {
        return activeNodes;
    }

    public void setActiveNodes(long activeNodes) {
        this.activeNodes = activeNodes;
    }

    public long getNrShards() {
        return nrShards;
    }

    public void setNrShards(long nrShards) {
        this.nrShards = nrShards;
    }

    public long getAverageRoundTime() {
        return averageRoundTime;
    }

    public void setAverageRoundTime(long averageRoundTime) {
        this.averageRoundTime = averageRoundTime;
    }

    public long getAverageNrTxPerBlock() {
        return averageNrTxPerBlock;
    }

    public void setAverageNrTxPerBlock(long averageNrTxPerBlock) {
        this.averageNrTxPerBlock = averageNrTxPerBlock;
    }

    public long getLiveNrTransactionsPerBlock() {
        return liveNrTransactionsPerBlock;
    }

    public void setLiveNrTransactionsPerBlock(long liveNrTransactionsPerBlock) {
        this.liveNrTransactionsPerBlock = liveNrTransactionsPerBlock;
    }
}
