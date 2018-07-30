package network.elrond.benchmark;

public class ShardStatistic {
    private long averageRoundTime;
    private long liveRoundTime;
    private long averageNrTxPerBlock;
    private long shardActiveNodes;
    private long currentShardNumber;
    private Double liveTps;
    private Double peakTps;
    private Double averageTps;
    private long liveNrTransactionsPerBlock;
    private long totalNrProcessedTransactions;
    private long currentBlockNonce;

    public Double getLiveTps() {
        return liveTps;
    }

    public void setLiveTps(Double liveTps) {
        this.liveTps = liveTps;
    }

    public Double getPeakTps() {
        return peakTps;
    }

    public void setPeakTps(Double peakTps) {
        this.peakTps = peakTps;
    }

    public Double getAverageTps() {
        return averageTps;
    }

    public void setAverageTps(Double averageTps) {
        this.averageTps = averageTps;
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

    public long getLiveRoundTime() {
        return liveRoundTime;
    }

    public void setLiveRoundTime(long liveRoundTime) {
        this.liveRoundTime = liveRoundTime;
    }

    public long getTotalNrProcessedTransactions() {
        return totalNrProcessedTransactions;
    }

    public void setTotalNrProcessedTransactions(long totalNrProcessedTransactions) {
        this.totalNrProcessedTransactions = totalNrProcessedTransactions;
    }

    public long getShardActiveNodes() {
        return shardActiveNodes;
    }

    public void setShardActiveNodes(long shardActiveNodes) {
        this.shardActiveNodes = shardActiveNodes;
    }

    public long getCurrentShardNumber() {
        return currentShardNumber;
    }

    public void setCurrentShardNumber(long currentShardNumber) {
        this.currentShardNumber = currentShardNumber;
    }

    public long getCurrentBlockNonce() {
        return currentBlockNonce;
    }

    public void setCurrentBlockNonce(long currentBlockNonce) {
        this.currentBlockNonce = currentBlockNonce;
    }
}
