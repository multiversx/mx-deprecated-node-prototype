package network.elrond.benchmark;

import java.util.HashMap;
import java.util.List;

public class BenchmarkManager {
    static BenchmarkManager instance;

    private BenchmarkManager(){};

    public static synchronized BenchmarkManager getInstance(){
        if(instance == null){
            instance = new BenchmarkManager();
        }
        return instance;
    }


    public BenchmarkResult getBenchmarkResult(List<StatisticsManager> statisticsManagers){
        BenchmarkResult res = new BenchmarkResult();
        res.getStatisticList().clear();
        Double peakTps = 0.0;
        Double liveTps = 0.0;
        Integer activeNodes=0;
        for (StatisticsManager statisticsManager : statisticsManagers) {
            ShardStatistic statistic = new ShardStatistic();
            statistic.setCurrentShardNumber(statisticsManager.getCurrentShardNumber());
            statistic.setShardActiveNodes(statisticsManager.getNumberNodesInShard());
            statistic.setAverageRoundTime(statisticsManager.getAverageRoundTime());
            statistic.setLiveNrTransactionsPerBlock(statisticsManager.getLiveNrTransactionsInBlock());
            statistic.setAverageNrTxPerBlock(statisticsManager.getAverageNrTransactionsInBlock());
            statistic.setAverageTps(statisticsManager.getAverageTps());
            statistic.setLiveTps(statisticsManager.getLiveTps());
            statistic.setLiveRoundTime(statisticsManager.getLiveRoundTime());
            statistic.setTotalNrProcessedTransactions(statisticsManager.getTotalNrProcessedTransactions());
            statistic.setCurrentBlockNonce(statisticsManager.getCurrentBlockNonce());
            Double shardPeakTps = maxPeakTpsPerShard.get(statisticsManager.getCurrentShardNumber());
            if(shardPeakTps==null || shardPeakTps < statisticsManager.getMaxTps()){
                shardPeakTps = statisticsManager.getMaxTps();
                maxPeakTpsPerShard.put(statisticsManager.getCurrentShardNumber(), shardPeakTps);
            }

            statistic.setPeakTps(shardPeakTps);
            peakTps += shardPeakTps;
            liveTps += statisticsManager.getLiveTps();
            activeNodes+=statisticsManager.getNumberNodesInShard();

            res.getStatisticList().add(statistic);
        }
        res.setGlobalPeakTps(peakTps);
        res.setGlobalLiveTps(liveTps);
        res.setNetworkActiveNodes(activeNodes);
        return res;
    }

    private double maxPeakTps = 0;
    private HashMap<Integer, Double> maxPeakTpsPerShard = new HashMap<Integer, Double>();
}
