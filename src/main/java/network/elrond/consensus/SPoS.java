package network.elrond.consensus;

import network.elrond.core.Util;

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
public class SPoS {
    /**
     * The method creates a node result of type EligibleListValidators that contains a clean-up version of the
     * whole eligible list validators (no negative ratings, stake equal or higher of minim stake, etc) and
     * computes minimum/maximum ratings, maximum stake.
     * @param eligibleList that contain validators to be included in cleaned up list
     * @return the node that holds the data
     */
    public static EligibleListValidators generateCleanupList(List<Validator> eligibleList) {
        //fetching parameters from validators (ie. min/max stake, min/max reputation, etc)

        EligibleListValidators result = new EligibleListValidators();

        if (eligibleList == null) {
            return (result);
        }

        for (int i = 0; i < eligibleList.size(); i++) {
            Validator v = eligibleList.get(i);

            //no double adds - JLS, performance issue detected 2018.09.05 18:30
//            if (result.list.contains(v))
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

            if (v.getRating() < result.minRating) {
                result.minRating = v.getRating();
            }

            if (v.getRating() > result.maxRating) {
                result.maxRating = v.getRating();
            }

            if (v.getStake().compareTo(result.maxStake) > 0) {
                result.maxStake = v.getStake();
            }

            result.list.add(new Validator(v));
        }

        return(result);
    }

    /**
     * The method computes the score for each validator and multiply its entrance in the output list
     * The score is an integer from 0 to Util.MAX_RATING and 0 means it appears once in the list, 1 twice and so on
     * @param cleanedUpListObject the objcet that holds the input data of the cleaned up list
     *                            (call method generateCleanupList first)
     * @return the weighted validators list
     */
    public static List<Validator> generateWeightedEligibleList(EligibleListValidators cleanedUpListObject) {

        if (cleanedUpListObject == null) {
            return (new ArrayList<Validator>());
        }

        List<Validator> tempList = copyList(cleanedUpListObject.list);

        //compute stake and rating score for each validator from temp list
        //and multiply temp table instances
        int tempSize = tempList.size();
        for (int i = 0; i < tempSize; i++) {
            Validator v = tempList.get(i);

            if (cleanedUpListObject.maxStake.compareTo(Util.MIN_STAKE) == 0) {
                v.setScoreStake(0);
            } else {
                //(current_stake - minStake) * Util.MAX_RATING / (maxStake - minStake)
                int sStake = (v.getStake().subtract(Util.MIN_STAKE)).multiply(BigInteger.valueOf(Util.MAX_RATING)).
                        divide(cleanedUpListObject.maxStake.subtract(Util.MIN_STAKE)).intValue();
                v.setScoreStake(sStake);
            }

            if (cleanedUpListObject.maxRating == cleanedUpListObject.minRating) {
                v.setScoreRating(0);
            } else {
                //(current_rating - minRating) * Util.MAX_RATING / (maxRating - minRating)
                int sRating = (v.getRating() - cleanedUpListObject.minRating) * Util.MAX_RATING /
                        (cleanedUpListObject.maxRating - cleanedUpListObject.minRating);
                v.setScoreRating(sRating);
            }

            int score = v.getScore();
            for (int j = 0; j < score; j++) {
                tempList.add(new Validator(v));
            }
        }

        return (tempList);
    }

    /**
     * The method returns a list of maximum Util.VERIFIER_GROUP_SIZE validators that will start the consensus process
     * The first validator in the list is the leader!!!
     * @param strRandomSource a String that will be used to select Util.VERIFIER_GROUP_SIZE from a weighted list
     * @param eligibleList the input weighted list (call first method generateWeightedEligibleList)
     * @param roundHeight the round height (ID)
     * @return the list of selected validator, first item being the leader
     */
    public static List<Validator> generateValidatorsList(String strRandomSource, List<Validator> eligibleList, BigInteger roundHeight) {
        //pick max Util.VERIFIER_GROUP_SIZE from eligible list
        //based on their stake, rating. Round r will rotate the lead

        List<Validator> tempList = new ArrayList<Validator>();
        if (eligibleList == null){
            return (tempList);
        }

        EligibleListValidators cleanedUpList = generateCleanupList(eligibleList);

        if (cleanedUpList.list.size() <= Util.VERIFIER_GROUP_SIZE) {
            //not enouch validators, copy them all
            tempList = copyList(cleanedUpList.list);

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
            BigInteger bi = new BigInteger(Util.SHA3.digest((strRandomSource + Integer.toString(nonce)).getBytes()));

            startIdx = bi.mod(BigInteger.valueOf(size)).intValue();

            boolean flagFound = false;
            while (!flagFound) {
                Validator v = weightedList.get(startIdx);

                if (tempList.contains(v)) {
                    startIdx++;
                    //prevent getting outside the list limits
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

    /**
     * Method used vor copying Lists of Validators. Method creates a new Validator for each item in the source list
     * @param src list to be copied
     * @return new list of Validators
     */
    public static List<Validator> copyList(List<Validator> src) {
        List<Validator> tempList = new ArrayList<Validator>();

        for (int i = 0; i < src.size(); i++) {
            tempList.add(new Validator(src.get(i)));
        }

        return (tempList);
    }

}
