package network.elrond.benchmark;

import network.elrond.application.AppState;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
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
    private Integer numberOfShards = 0;
    private Integer numberNodesInShard = 0;
    private Integer numberNodesInNetwork = 0;

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

    private long averageRoundTime = 0;
    private long liveRoundTime = 0;

    private long totalProcessedTransactions = 0;

    public StatisticsManager(ElrondSystemTimer timer) {
        this.timer = timer;
        this.startMillis = timer.getCurrentTime();
        currentMillis = startMillis;
    }

    public void updateNetworkStats(AppState state) {
        currentShardNumber = state.getShard().getIndex();
        numberOfShards = AppServiceProvider.getShardingService().getNumberOfShards();
        numberNodesInNetwork = AppShardingManager.instance().getNumberNodesInNetwork(state);
        numberNodesInShard = AppShardingManager.instance().getNumberNodesInShard(state);
    }

    public void addStatistic(Statistic statistic) {
        logger.traceEntry("params: {}", statistic);

        long ellapsedMillis = statistic.getCurrentTimeMillis() - currentMillis;
        liveRoundTime = ellapsedMillis;
        currentMillis = statistic.getCurrentTimeMillis();

        statistics.add(statistic);
        if (statistics.size() > maxStatistics) {
            statistics.remove(0);
        }

        totalProcessedTransactions += statistic.getNrTransactionsInBlock();
        liveTps = statistic.getNrTransactionsInBlock() * 1000.0 / ellapsedMillis;
        logger.trace("currentTps is " + liveTps);
        ComputeTps(liveTps);

        liveNrTransactionsInBlock = statistic.getNrTransactionsInBlock();
        computeNrTransactionsInBlock(liveNrTransactionsInBlock);

        computeAverageRoundTime(ellapsedMillis);

        currentIndex++;
        logger.traceExit();
    }

    private void computeAverageRoundTime(long timeDifference) {
        averageRoundTime = (averageRoundTime * currentIndex + timeDifference) / (currentIndex + 1);
        logger.trace("averageNrTransactionsInBlock is " + averageNrTransactionsInBlock);
    }

    private void ComputeTps(Double currentTps) {
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

        averageNrTransactionsInBlock = (averageNrTransactionsInBlock * currentIndex + currentNrTransactionsInBlock) / (currentIndex + 1);
        logger.trace("averageNrTransactionsInBlock is " + averageNrTransactionsInBlock);
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

    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    public Integer getNumberNodesInShard() {
        return numberNodesInShard;
    }

    public Integer getNumberNodesInNetwork() {
        return numberNodesInNetwork;
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
}
