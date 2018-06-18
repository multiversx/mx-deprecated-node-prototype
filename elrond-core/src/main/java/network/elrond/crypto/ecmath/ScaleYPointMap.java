package network.elrond.crypto.ecmath;

public class ScaleYPointMap implements ECPointMap
{
    protected final ECFieldElement scale;

    public ScaleYPointMap(ECFieldElement scale)
    {
        this.scale = scale;
    }

    public ECPoint map(ECPoint p)
    {
        return p.scaleY(scale);
    }
}
