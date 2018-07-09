package network.elrond.benchmark;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkResult {

    private long networkActiveNodes;
    private long nrShards;

    private Double globalPeakTps;
    private Double globalLiveTps;

    private List<ShardStatistic> statisticList = new ArrayList<>();

    public long getNetworkActiveNodes() {
        return networkActiveNodes;
    }

    public void setNetworkActiveNodes(long networkActiveNodes) {
        this.networkActiveNodes = networkActiveNodes;
    }

    public long getNrShards() {
        return nrShards;
    }

    public void setNrShards(long nrShards) {
        this.nrShards = nrShards;
    }

    public List<ShardStatistic> getStatisticList() {
        return statisticList;
    }

    public Double getGlobalLiveTps() {
        return globalLiveTps;
    }

    public void setGlobalLiveTps(Double globalLiveTps) {
        this.globalLiveTps = globalLiveTps;
    }

    public Double getGlobalPeakTps() {
        return globalPeakTps;
    }

    public void setGlobalPeakTps(Double globalPeakTps) {
        this.globalPeakTps = globalPeakTps;
    }
}