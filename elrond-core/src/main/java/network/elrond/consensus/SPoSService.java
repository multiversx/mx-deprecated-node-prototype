package network.elrond.consensus;

import java.math.BigInteger;
import java.util.List;

public interface SPoSService {
    EligibleListValidators generateCleanupList(List<Validator> eligibleList);
    List<Validator> generateWeightedEligibleList(EligibleListValidators cleanedUpListObject);
    List<Validator> generateValidatorsList(String strRandomSource, List<Validator> eligibleList, BigInteger roundHeight);
}
