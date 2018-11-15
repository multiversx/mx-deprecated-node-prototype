package network.elrond.benchmark;

import network.elrond.application.AppState;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import network.elrond.util.time.ElrondSystemTimer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatisticsManager implements Serializable {
    private static final Logger logger = LogManager.getLogger(StatisticsManager.class);
    private ElrondSystemTimer timer;
    private static final int maxStatistics = 100;
    private List<Statistic> statistics = new ArrayList<>();

    private Integer currentShardNumber = 0;
    private Integer numberNodesInShard = 0;

    private long currentIndex = 0;
    private long currentMillis = 0;
    private long startMillis = 0;

    private Double averageTps = 0.0;
    private Double maxTps = 0.0;
    private Double minTps = Double.MAX_VALUE;
    private Double liveTps = 0.0;

    private long averageNrTransactionsInBlock = 0;
    private long maxNrTransactionsInBlock = 0;
    private long minNrTransactionsInBlock = Integer.MAX_VALUE;
    private long liveNrTransactionsInBlock = 0;
    private long currentBlockNonce = 0;

    private long averageRoundTime = 0;
    private long liveRoundTime = 0;

    private long totalProcessedTransactions = 0;

    public StatisticsManager(ElrondSystemTimer timer, int currentShardNumber) {
        this.timer = timer;
        this.startMillis = timer.getCurrentTime();
        currentMillis = startMillis;
        this.currentShardNumber = currentShardNumber;
    }

    public void updateNetworkStats(AppState state) {
        numberNodesInShard = AppShardingManager.instance().getNumberNodesInShard(state);
    }

    List<Statistic> currentStatistics = new ArrayList<>();

    public void addStatistic(Statistic statistic){
        currentStatistics.add(statistic);
    }

    public void processStatistics() {
        logger.traceEntry("params: {}", currentStatistics);

        if(currentStatistics == null || currentStatistics.isEmpty()){
            return;
        }
        long roundTimeDuration = AppServiceProvider.getChronologyService().getRoundTimeDuration();

        long newTime  = timer.getCurrentTime();
        long ellapsedMillis = newTime - currentMillis;
        liveRoundTime = ellapsedMillis;
        currentMillis = newTime;

        liveRoundTime = roundTimeDuration;

        statistics.addAll(currentStatistics);
        if (statistics.size() > maxStatistics) {
            statistics.remove(0);
        }

        long transactionsCount = 0;
        long maxTransactionsProcessed = 0;
        long maxBlockNonce = currentBlockNonce;
        for(Statistic stat : currentStatistics){
            long currentTxInBlock = stat.getNrTransactionsInBlock();
            if(currentTxInBlock > maxTransactionsProcessed){
                maxTransactionsProcessed = currentTxInBlock;
            }
            transactionsCount += currentTxInBlock;

            long currentBlockNumber = stat.getCurrentBlockNonce();
            if(currentBlockNumber > maxBlockNonce){
                maxBlockNonce = currentBlockNumber + 1;
            }
            logger.info("Statistics for block {}: {} transactions", currentBlockNumber, currentTxInBlock);
        }

        totalProcessedTransactions += transactionsCount;
        liveNrTransactionsInBlock = maxTransactionsProcessed;

        liveTps = maxTransactionsProcessed * 1000.0 / liveRoundTime;//get the max TPS from all statistics

        logger.info("Live Round Time is: " + liveRoundTime + " and processed " + maxTransactionsProcessed
                + " transactions at a TPS of: " + liveTps);

        logger.trace("currentTps is " + liveTps);

        computeTps(liveTps);
        computeNrTransactionsInBlock(liveNrTransactionsInBlock);
        computeAverageRoundTime(ellapsedMillis);

        currentBlockNonce = maxBlockNonce;

        currentIndex++;
        currentStatistics.clear();
        logger.traceExit();
    }

    private void computeAverageRoundTime(long timeDifference) {
        averageRoundTime = (averageRoundTime * currentIndex + timeDifference) / (currentIndex + 1);
        logger.trace("averageNrTransactionsInBlock is " + averageNrTransactionsInBlock);
    }

    private void computeTps(Double currentTps) {
        if (maxTps < currentTps) {
            maxTps = currentTps;
        }

        if (minTps > currentTps) {
            minTps = currentTps;
        }

        averageTps = (totalProcessedTransactions * 1000.0) / (timer.getCurrentTime() - startMillis);
        logger.trace("averageTps is " + averageTps);
    }

    private void computeNrTransactionsInBlock(long currentNrTransactionsInBlock) {
        if (maxNrTransactionsInBlock < currentNrTransactionsInBlock) {
            maxNrTransactionsInBlock = currentNrTransactionsInBlock;
        }

        if (minNrTransactionsInBlock > currentNrTransactionsInBlock) {
            minNrTransactionsInBlock = currentNrTransactionsInBlock;
        }

        if(currentBlockNonce > 0) {
            averageNrTransactionsInBlock = totalProcessedTransactions / currentBlockNonce;
        }

        logger.trace("averageNrTransactionsInProposedBlock is " + averageNrTransactionsInBlock);
    }

    public Double getAverageTps() {
        return averageTps;
    }

    public Double getMaxTps() {
        return maxTps;
    }

    public Double getMinTps() {
        return minTps;
    }

    public Double getLiveTps() {
        return liveTps;
    }

    public long getAverageNrTransactionsInBlock() {
        return averageNrTransactionsInBlock;
    }

    public long getMaxNrTransactionsInBlock() {
        return maxNrTransactionsInBlock;
    }

    public long getMinNrTransactionsInBlock() {
        return minNrTransactionsInBlock;
    }

    public long getLiveNrTransactionsInBlock() {
        return liveNrTransactionsInBlock;
    }

    public Integer getNumberNodesInShard() {
        return numberNodesInShard;
    }

    public long getAverageRoundTime() {
        return averageRoundTime;
    }

    public long getLiveRoundTime() {
        return liveRoundTime;
    }

    public long getTotalNrProcessedTransactions() {
        return totalProcessedTransactions;
    }

    public Integer getCurrentShardNumber() {
        return currentShardNumber;
    }

    public long getCurrentBlockNonce() { return currentBlockNonce;     }
}
