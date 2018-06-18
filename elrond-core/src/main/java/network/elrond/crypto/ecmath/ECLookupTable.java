package network.elrond.crypto.ecmath;

public interface ECLookupTable
{
    int getSize();
    ECPoint lookup(int index);
}
