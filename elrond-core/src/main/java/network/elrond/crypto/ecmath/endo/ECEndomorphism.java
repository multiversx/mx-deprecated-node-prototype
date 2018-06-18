package network.elrond.crypto.ecmath.endo;

import network.elrond.crypto.ecmath.ECPointMap;

public interface ECEndomorphism
{
    ECPointMap getPointMap();

    boolean hasEfficientPointMap();
}
