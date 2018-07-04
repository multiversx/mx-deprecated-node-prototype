package network.elrond.benchmark;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticsMangerTest {
    private StatisticsManager statisticService;
    private ElrondSystemTimer timer;
    @Before
    public void SetUp(){
        timer = mock(ElrondSystemTimer.class);
        when(timer.getCurrentTime())
                .thenReturn((long) 1000)
                .thenReturn((long) 2000)
                .thenReturn((long) 3000)
                .thenReturn((long) 4000)
                .thenReturn((long) 5000);
        statisticService = new StatisticsManager(timer);
    }

    @Test
    public void TestHappyPathStatisticsManager(){

        Statistic statistic = mock(Statistic.class);
        when(statistic.getCurrentTimeMillis()).thenReturn((long)2000);
        when(statistic.getNrTransactionsInBlock()).thenReturn((long)100);

        statisticService.addStatistic(statistic);

        Assert.assertEquals((Double)100.0, statisticService.getAverageTps());
        Assert.assertEquals((Double)100.0, statisticService.getMaxTps());
        Assert.assertEquals((Double)100.0, statisticService.getMinTps());

        Statistic statistic2= mock(Statistic.class);
        when(statistic2.getCurrentTimeMillis()).thenReturn((long)3000);
        when(statistic2.getNrTransactionsInBlock()).thenReturn((long)200);

        statisticService.addStatistic(statistic2);

        Assert.assertEquals((Double)150.0, statisticService.getAverageTps());
        Assert.assertEquals((Double)200.0, statisticService.getMaxTps());
        Assert.assertEquals((Double)100.0, statisticService.getMinTps());

        Statistic statistic3= mock(Statistic.class);
        when(statistic3.getCurrentTimeMillis()).thenReturn((long)4000);
        when(statistic3.getNrTransactionsInBlock()).thenReturn((long)300);

        statisticService.addStatistic(statistic3);

        Assert.assertEquals((Double)200.0, statisticService.getAverageTps());
        Assert.assertEquals((Double)300.0, statisticService.getMaxTps());
        Assert.assertEquals((Double)100.0, statisticService.getMinTps());
    }
}
