package network.elrond.benchmark;

public interface StatisticService {
    void addStatistic(Statistic statistic);

    Double getAverageTps();

    Double getMaxTps();

    Double getMinTps();

    Double getLiveTps();

    long getAverageNrTransactionsInBlock();

    long getMaxNrTransactionsInBlock();

    long getMinNrTransactionsInBlock();

    long getLiveNrTransactionsInBlock();

    long getAverageRoundTime();

    long getLiveRoundTime();

    long getTotalNrProcessedTransactions();
}
