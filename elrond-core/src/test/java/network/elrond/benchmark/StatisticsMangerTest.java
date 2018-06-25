package network.elrond.benchmark;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StatisticsMangerTest {
    private StatisticsManager statisticsManager;

    @Before
    public void SetUp(){
        statisticsManager = new StatisticsManager();
    }

    @Test
    public void TestHappyPathStatisticsManager(){
        Statistic statistic = new Statistic();
        statistic.setTps(100);
        statistic.setNrTransactionsInBlock(1000);

        statisticsManager.addStatistic(statistic);

        Assert.assertEquals(100, statisticsManager.getAverageTps());
        Assert.assertEquals(100, statisticsManager.getMaxTps());
        Assert.assertEquals(100, statisticsManager.getMinTps());

        Statistic statistic2= new Statistic();
        statistic2.setTps(200);
        statistic2.setNrTransactionsInBlock(2000);

        statisticsManager.addStatistic(statistic2);

        Assert.assertEquals(150, statisticsManager.getAverageTps());
        Assert.assertEquals(200, statisticsManager.getMaxTps());
        Assert.assertEquals(100, statisticsManager.getMinTps());

        Statistic statistic3= new Statistic();
        statistic3.setTps(300);
        statistic3.setNrTransactionsInBlock(3000);

        statisticsManager.addStatistic(statistic3);

        Assert.assertEquals(200, statisticsManager.getAverageTps());
        Assert.assertEquals(300, statisticsManager.getMaxTps());
        Assert.assertEquals(100, statisticsManager.getMinTps());
    }
}
