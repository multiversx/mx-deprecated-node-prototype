package network.elrond.consensus;

public interface ValidatorService {
    int computeValidatorScore(Validator val);
}
