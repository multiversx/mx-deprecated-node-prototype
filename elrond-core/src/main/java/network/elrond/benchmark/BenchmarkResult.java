package network.elrond.benchmark;

public class BenchmarkResult {
    private Double liveTps;
    private Double maxTps;
    private Double averageTps;
    private Integer activeNodes;
    private Integer nrShards;
    private Double averageRoundTime;
    private Double averageTransactionsPerBlock;

    public Double getLiveTps() {
        return liveTps;
    }

    public void setLiveTps(Double liveTps) {
        this.liveTps = liveTps;
    }

    public Double getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(Double maxTps) {
        this.maxTps = maxTps;
    }

    public Double getAverageTps() {
        return averageTps;
    }

    public void setAverageTps(Double averageTps) {
        this.averageTps = averageTps;
    }

    public Integer getActiveNodes() {
        return activeNodes;
    }

    public void setActiveNodes(Integer activeNodes) {
        this.activeNodes = activeNodes;
    }

    public Integer getNrShards() {
        return nrShards;
    }

    public void setNrShards(Integer nrShards) {
        this.nrShards = nrShards;
    }

    public Double getAverageRoundTime() {
        return averageRoundTime;
    }

    public void setAverageRoundTime(Double averageRoundTime) {
        this.averageRoundTime = averageRoundTime;
    }

    public Double getAverageTransactionsPerBlock() {
        return averageTransactionsPerBlock;
    }

    public void setAverageTransactionsPerBlock(Double averageTransactionsPerBlock) {
        this.averageTransactionsPerBlock = averageTransactionsPerBlock;
    }
}
