package network.elrond.benchmark;

public interface StatisticService {
    void addStatistic(Statistic statistic);

    //void ComputeTps(long currentTps);

    //void computeNrTransactionsInBlock(long currentNrTransactionsInBlock);

    Double getAverageTps();

    Double getMaxTps();

    Double getMinTps();

    Double getLiveTps();

    long getAverageNrTransactionsInBlock();

    long getMaxNrTransactionsInBlock();

    long getMinNrTransactionsInBlock();

    long getLiveNrTransactionsInBlock();

    //Statistic getCurrentStatistic();

    long getAverageRoundTime();

    long getLiveRoundTime();

    long getTotalNrProcessedTransactions();
}
