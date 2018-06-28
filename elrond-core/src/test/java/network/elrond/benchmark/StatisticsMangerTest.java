package network.elrond.benchmark;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticsMangerTest {
    private StatisticsManager statisticService;

    @Before
    public void SetUp(){
        statisticService = new StatisticsManager(1000);
    }

    @Test
    @Ignore
    //TO DO: fixing
    public void TestHappyPathStatisticsManager(){

        Statistic statistic = mock(Statistic.class);
        when(statistic.getCurrentTimeMillis()).thenReturn((long)2000);
        when(statistic.getNrTransactionsInBlock()).thenReturn((long)100);

        statisticService.addStatistic(statistic);

        Assert.assertEquals((Double)100.0, statisticService.getAverageTps());
        Assert.assertEquals((Double)100.0, statisticService.getMaxTps());
        Assert.assertEquals((Double)100.0, statisticService.getMinTps());

        Statistic statistic2= mock(Statistic.class);
        when(statistic.getCurrentTimeMillis()).thenReturn((long)3000);
        when(statistic.getNrTransactionsInBlock()).thenReturn((long)200);

        statisticService.addStatistic(statistic2);

        Assert.assertEquals((Double)150.0, statisticService.getAverageTps());
        Assert.assertEquals((Double)200.0, statisticService.getMaxTps());
        Assert.assertEquals((Double)100.0, statisticService.getMinTps());

        Statistic statistic3= mock(Statistic.class);
        when(statistic.getCurrentTimeMillis()).thenReturn((long)4000);
        when(statistic.getNrTransactionsInBlock()).thenReturn((long)300);

        statisticService.addStatistic(statistic3);

        Assert.assertEquals((Double)200.0, statisticService.getAverageTps());
        Assert.assertEquals((Double)300.0, statisticService.getMaxTps());
        Assert.assertEquals((Double)100.0, statisticService.getMinTps());
    }
}
