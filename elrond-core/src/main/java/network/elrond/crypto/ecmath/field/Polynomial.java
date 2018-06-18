package network.elrond.crypto.ecmath.field;

public interface Polynomial
{
    int getDegree();

//    BigInteger[] getCoefficients();

    int[] getExponentsPresent();

//    Term[] getNonZeroTerms();
}
