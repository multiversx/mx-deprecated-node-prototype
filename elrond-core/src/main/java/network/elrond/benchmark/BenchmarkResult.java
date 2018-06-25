package network.elrond.benchmark;

public class BenchmarkResult {
    private long liveTps;
    private long maxTps;
    private long averageTps;
    private long activeNodes;
    private long nrShards;
    private Double averageRoundTime;
    private long averageNrTransactionsPerBlock;
    private long liveNrTransactionsPerBlock;

    public long getLiveTps() {
        return liveTps;
    }

    public void setLiveTps(long liveTps) {
        this.liveTps = liveTps;
    }

    public long getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(long maxTps) {
        this.maxTps = maxTps;
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

    public Double getAverageRoundTime() {
        return averageRoundTime;
    }

    public void setAverageRoundTime(Double averageRoundTime) {
        this.averageRoundTime = averageRoundTime;
    }

    public long getAverageNrTransactionsPerBlock() {
        return averageNrTransactionsPerBlock;
    }

    public void setAverageNrTransactionsPerBlock(long averageNrTransactionsPerBlock) {
        this.averageNrTransactionsPerBlock = averageNrTransactionsPerBlock;
    }

    public long getLiveNrTransactionsPerBlock() {
        return liveNrTransactionsPerBlock;
    }

    public void setLiveNrTransactionsPerBlock(long liveNrTransactionsPerBlock) {
        this.liveNrTransactionsPerBlock = liveNrTransactionsPerBlock;
    }
}
