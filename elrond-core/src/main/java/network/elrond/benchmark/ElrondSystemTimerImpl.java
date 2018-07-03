package network.elrond.benchmark;

public class ElrondSystemTimerImpl implements ElrondSystemTimer{
    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
