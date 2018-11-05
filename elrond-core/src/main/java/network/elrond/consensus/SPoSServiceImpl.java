package network.elrond.consensus;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The SpoS class implements the Secure Proof of Stake algorithm
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public class SPoSServiceImpl implements SPoSService {
    /**
     * The method creates a node result of type EligibleListValidators that contains a clean-up version of the
     * whole eligible listToTable validators (no negative ratings, stake equal or higher of minim stake, etc) and
     * computes minimum/maximum ratings, maximum stake.
     * @param eligibleList that contain validators to be included in cleaned up listToTable
     * @return the node that holds the data
     */
    @Override
	public EligibleListValidators generateCleanupList(List<Validator> eligibleList) {
        //fetching parameters from validators (ie. min/max stake, min/max reputation, etc)

        EligibleListValidators result = new EligibleListValidators();

        if (eligibleList == null) {
            return (result);
        }

        for (int i = 0; i < eligibleList.size(); i++) {
            Validator v = eligibleList.get(i);

            //no double adds - JLS, performance issue detected 2018.09.05 18:30
//            if (result.listToTable.contains(v))
//            {
//                continue;
//            }

            //if validator has no stake in current round, skip it
            if (v.getStake().compareTo(Util.MIN_STAKE) < 0) {
                continue;
            }

            //if validator has a negative rating (malicious?), skip it
            if (v.getRating() < 0) {
                continue;
            }

            if (v.getRating() < result.getMinRating()) {
                result.setMinRating(v.getRating());
            }

            if (v.getRating() > result.getMaxRating()) {
                result.setMaxRating(v.getRating());
            }

            if (v.getStake().compareTo(result.getMaxStake()) > 0) {
                result.setMaxStake(v.getStake());
            }

            result.addValidator(new Validator(v));
        }

        return(result);
    }

    /**
     * The method computes the score for each validator and multiply its entrance in the output listToTable
     * The score is an integer from 0 to Util.MAX_SCORE and 0 means it appears once in the listToTable, 1 twice and so on
     * @param cleanedUpListObject the objcet that holds the input data of the cleaned up listToTable
     *                            (call method generateCleanupList first)
     * @return the weighted validators listToTable
     */
    @Override
	public List<Validator> generateWeightedEligibleList(EligibleListValidators cleanedUpListObject) {

        if (cleanedUpListObject == null) {
            return (new ArrayList<Validator>());
        }

        ValidatorService vs = AppServiceProvider.getValidatorService();

        List<Validator> tempList = cleanedUpListObject.getValidatorListCopy();

        //compute stake and rating score for each validator from temp listToTable
        //and multiply temp table instances
        int tempSize = tempList.size();
        for (int i = 0; i < tempSize; i++) {
            Validator v = tempList.get(i);

            if (cleanedUpListObject.getMaxStake().compareTo(Util.MIN_STAKE) == 0) {
                v.setScoreStake(0);
            } else {
                //(current_stake - minStake) * Util.MAX_SCORE / (maxStake - minStake)
                int sStake = (v.getStake().subtract(Util.MIN_STAKE)).multiply(BigInteger.valueOf(Util.MAX_SCORE)).
                        divide(cleanedUpListObject.getMaxStake().subtract(Util.MIN_STAKE)).intValue();
                v.setScoreStake(sStake);
            }

            if (cleanedUpListObject.getMaxRating() == cleanedUpListObject.getMinRating()) {
                v.setScoreRating(0);
            } else {
                //(current_rating - minRating) * Util.MAX_SCORE / (maxRating - minRating)
                int sRating = (v.getRating() - cleanedUpListObject.getMinRating()) * Util.MAX_SCORE /
                        (cleanedUpListObject.getMaxRating() - cleanedUpListObject.getMinRating());
                v.setScoreRating(sRating);
            }

            int score = vs.computeValidatorScore(v);
            for (int j = 0; j < score; j++) {
                tempList.add(new Validator(v));
            }
        }

        return (tempList);
    }

    /**
     * The method returns a listToTable of maximum Util.VERIFIER_GROUP_SIZE validators that will start the consensus process
     * The first validator in the listToTable is the leader!!!
     * @param strRandomSource a String that will be used to select Util.VERIFIER_GROUP_SIZE from a weighted listToTable
     * @param eligibleList the input weighted listToTable (call first method generateWeightedEligibleList)
     * @param roundHeight the round height (ID)
     * @return the listToTable of selected validator, first item being the leader
     */
    @Override
	public List<Validator> generateValidatorsList(String strRandomSource, List<Validator> eligibleList, BigInteger roundHeight) {
        //pick max Util.VERIFIER_GROUP_SIZE from eligible listToTable
        //based on their stake, rating. Round r will rotate the lead

        List<Validator> tempList = new ArrayList<Validator>();
        if (eligibleList == null){
            return (tempList);
        }

        EligibleListValidators cleanedUpList = generateCleanupList(eligibleList);

        if (cleanedUpList.getNrValidators() <= Util.VERIFIER_GROUP_SIZE) {
            //not enouch validators, copy them all
            tempList = cleanedUpList.getValidatorListCopy();

            //rotate leader
            if ((roundHeight.compareTo(BigInteger.ZERO) > 0) && (tempList.size() > 1)) {
                //rotate the leader based on the round r
                int leaderIdx = roundHeight.mod(BigInteger.valueOf(tempList.size())).intValue();

                Collections.swap(tempList, 0, leaderIdx);
            }

            return(tempList);
        }

        List<Validator> weightedList = generateWeightedEligibleList(cleanedUpList);

        //it's a kind of magic (PoW) :)
        int nonce = 1;
        int size = weightedList.size();
        int startIdx = 0;
        while (tempList.size() < Util.VERIFIER_GROUP_SIZE) {
            BigInteger bi = new BigInteger(Util.SHA3.get().digest((strRandomSource + Integer.toString(nonce)).getBytes()));

            startIdx = bi.mod(BigInteger.valueOf(size)).intValue();

            boolean flagFound = false;
            while (!flagFound) {
                Validator v = weightedList.get(startIdx);

                if (tempList.contains(v)) {
                    startIdx++;
                    //prevent getting outside the listToTable limits
                    startIdx = startIdx % size;
                    continue;
                }

                tempList.add(new Validator(v));
                flagFound = true;
            }

            nonce++;
        }

        //rotate leader
        if ((roundHeight.compareTo(BigInteger.ZERO) > 0) && (tempList.size() > 1)) {
            //rotate the leader based on the round r
            int leaderIdx = roundHeight.mod(BigInteger.valueOf(tempList.size())).intValue();

            Collections.swap(tempList, 0, leaderIdx);
        }

        return(tempList);
    }

}
