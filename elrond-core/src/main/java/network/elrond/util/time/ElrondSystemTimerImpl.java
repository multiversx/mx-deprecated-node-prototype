package network.elrond.util.time;

import java.io.Serializable;

public class ElrondSystemTimerImpl implements ElrondSystemTimer, Serializable {
    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
