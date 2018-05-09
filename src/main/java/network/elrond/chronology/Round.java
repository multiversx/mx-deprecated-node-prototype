package network.elrond.chronology;

import network.elrond.consensus.Validator;
import network.elrond.core.Util;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Round {

    private List<Validator> listValidators;
    private Epoch parentEpoch;
    private BigInteger roundHeight;

    private DigestSHA3 sha3;

    public Round(Epoch parentEpoch) {
        listValidators = new ArrayList<Validator>();
        this.parentEpoch = parentEpoch;
        if (this.parentEpoch == null) {
            roundHeight = BigInteger.ZERO;
        } else {
            if (this.parentEpoch.getLastRound() == null) {
                roundHeight = BigInteger.ZERO;
            } else {
                roundHeight = this.parentEpoch.getLastRound().roundHeight.add(BigInteger.ONE);
            }
        }

        sha3 = new DigestSHA3(256);
    }

    public Epoch getParentEpoch() {
        return (parentEpoch);
    }

    public List<Validator> getListValidators() {
        return (listValidators);
    }

    public BigInteger roundHeight() {
        return (roundHeight);
    }

    public List<Validator> generateRoundValidatorList() {
        List<Validator> tempList = new ArrayList<Validator>();

        if (parentEpoch == null) {
            return (tempList);
        }

//        if (parentEpoch.getEligibleList().size() <= Util.VERIFIER_GROUP_SIZE) {
//            return (copyList(parentEpoch.getEligibleList()));
//        }

        //fetching parameters from validators (ie. min/max stake, min/max reputation, etc)
        //an build a new list out of those parameters from which to select the validators

        //so. step 1: fetching parameters and adding eligible nodes in a temp table
        //BigInteger minStake = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger maxStake = BigInteger.ZERO;

        int minRating = Util.MAX_RATING;
        int maxRating = 0;

        for (int i = 0; i < parentEpoch.getEligibleList().size(); i++) {
            Validator v = parentEpoch.getEligibleList().get(i);

            //if validator has no stake in current round, skip it
            if (v.getStake().compareTo(Util.MIN_STAKE) < 0) {
                continue;
            }

            //if validator has a negative rating (malicious?), skip it
            if (v.getRating() < 0) {
                continue;
            }

            if (v.getRating() < minRating) {
                minRating = v.getRating();
            }

            if (v.getRating() > maxRating) {
                maxRating = v.getRating();
            }

//            if (v.getStake().compareTo(minStake) < 0) {
//                minStake = v.getStake();
//            }

            if (v.getStake().compareTo(maxStake) > 0) {
                maxStake = v.getStake();
            }

            tempList.add(new RoundValidator(v));
        }

        if (tempList.size() < Util.VERIFIER_GROUP_SIZE) {
            return (copyList(tempList));
        }

        //step 2. compute stake and rating score for each validator from temp list
        //and multiply temp table instances
        int tempSize = tempList.size();
        for (int i = 0; i < tempSize; i++) {
            RoundValidator rv = (RoundValidator) tempList.get(i);

            if (maxStake.compareTo(Util.MIN_STAKE) == 0) {
                rv.setScoreStake(0);
            } else {
                //(current_stake - minStake) * Util.MAX_RATING / (maxStake - minStake)
                int sStake = (rv.getStake().subtract(Util.MIN_STAKE)).multiply(BigInteger.valueOf(Util.MAX_RATING)).
                        divide(maxStake.subtract(Util.MIN_STAKE)).intValue();
                rv.setScoreStake(sStake);
            }

            if (maxRating == minRating) {
                rv.setScoreRating(0);
            } else {
                //(current_rating - minRating) * Util.MAX_RATING / (maxRating - minRating)
                int sRating = (rv.getRating() - minRating) * Util.MAX_RATING / (maxRating - minRating);
                rv.setScoreRating(sRating);
            }

            int score = rv.getScore();
            for (int j = 0; j < score; j++) {
                tempList.add(rv.Clone());
            }
        }

        return (tempList);
    }


    public void rebuildValidatorsList(String strRandomSource) {
        //pick max Util.VERIFIER_GROUP_SIZE from eligible list
        //based on their stake, rating. Round r will rotate the lead

        listValidators.clear();

//        if (parentEpoch.getEligibleList().size() <= Util.VERIFIER_GROUP_SIZE) {
//            //plain copy and return because the size of eligible nodes does not suffice the minimum no. of validators
//            for (int i = 0; i < parentEpoch.getEligibleList().size(); i++) {
//                listValidators.add(new Validator(parentEpoch.getEligibleList().get(i)));
//            }
//
//            if (roundHeight.compareTo(BigInteger.ZERO) > 0) {
//                //rotate the leader based on the round r
//                int leaderIdx = roundHeight.mod(BigInteger.valueOf(listValidators.size())).intValue();
//
//                Collections.swap(listValidators, 0, leaderIdx);
//            }
//
//            return;
//        }

        List<Validator> tempList = generateRoundValidatorList();

        if (tempList.size() == 0){
            return;
        }

        //it's a kind of PoW :)
        int nonce = 1;
        int size = tempList.size();
        int startIdx = 0;
        while (listValidators.size() < Util.VERIFIER_GROUP_SIZE) {
            BigInteger bi = new BigInteger(sha3.digest((strRandomSource + Integer.toString(nonce)).getBytes()));

            startIdx = bi.mod(BigInteger.valueOf(size)).intValue();

            boolean flagFound = false;
            while (!flagFound) {
                Validator v = tempList.get(startIdx);

                if (listValidators.contains(v)) {
                    startIdx++;
                    //prevent getting outside the list limits
                    startIdx = startIdx % size;
                    continue;
                }

                listValidators.add(new Validator(v));
                flagFound = true;
            }

            nonce++;
        }
    }

    private List<Validator> copyList(List<Validator> src) {
        List<Validator> tempList = new ArrayList<Validator>();

        for (int i = 0; i < src.size(); i++) {
            tempList.add(new Validator(src.get(i)));
        }

        return (tempList);
    }
}
