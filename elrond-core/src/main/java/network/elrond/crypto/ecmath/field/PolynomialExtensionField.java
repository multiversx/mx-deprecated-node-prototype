package network.elrond.crypto.ecmath.field;

public interface PolynomialExtensionField extends ExtensionField
{
    Polynomial getMinimalPolynomial();
}
