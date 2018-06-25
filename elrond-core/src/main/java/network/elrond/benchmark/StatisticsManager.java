package network.elrond.benchmark;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class StatisticsManager {
    private static final Logger logger = LogManager.getLogger(StatisticsManager.class);

    private static final int maxStatistics = 100;
    private List<Statistic> statistics = new ArrayList<>();



    private long currentIndex = 0;
    private Statistic currentStatistic;

    private long averageTps = 0;
    private long maxTps = 0;
    private long minTps = Integer.MAX_VALUE;

    private long averageNrTransactionsInBlock = 0;
    private long maxNrTransactionsInBlock = 0;
    private long minNrTransactionsInBlock = Integer.MAX_VALUE;

    public void addStatistic(Statistic statistic) {
        logger.traceEntry("params: {}", statistic);

        currentStatistic = statistic;

        statistics.add(statistic);
        if (statistics.size() > maxStatistics){
            statistics.remove(0);
        }

        long currentTps = statistic.getTps();
        logger.trace("currentTps is " + currentTps);
        ComputeTps(currentTps);

        long currentNrTransactionsInBlock = statistic.getNrTransactionsInBlock();
        computeNrTransactionsInBlock(currentNrTransactionsInBlock);

        logger.traceExit();
    }

    public void ComputeTps(long currentTps) {
        if(maxTps <currentTps){
            maxTps = currentTps;
        }

        if(minTps > currentTps){
            minTps = currentTps;
        }

        averageTps = (averageTps*currentIndex + currentTps) / ++currentIndex;
        logger.trace("averageTps is " + averageTps);
    }

    public void computeNrTransactionsInBlock(long currentNrTransactionsInBlock) {
        if(maxNrTransactionsInBlock <currentNrTransactionsInBlock){
            maxNrTransactionsInBlock = currentNrTransactionsInBlock;
        }

        if(minNrTransactionsInBlock > currentNrTransactionsInBlock){
            minNrTransactionsInBlock = currentNrTransactionsInBlock;
        }

        averageNrTransactionsInBlock = (averageNrTransactionsInBlock *currentIndex + currentNrTransactionsInBlock) / ++currentIndex;
        logger.trace("averageNrTransactionsInBlock is " + averageNrTransactionsInBlock);
    }

    public long getAverageTps() {
        return averageTps;
    }

    public long getMaxTps() {
        return maxTps;
    }

    public long getMinTps() {
        return minTps;
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

    public Statistic getCurrentStatistic() {
        return currentStatistic;
    }
}
