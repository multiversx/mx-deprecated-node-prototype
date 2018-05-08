package network.elrond.chronology;

import network.elrond.consensus.Validator;
import network.elrond.core.Util;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

import java.math.BigInteger;
import java.util.ArrayList;
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
        }
        else{
            if (this.parentEpoch.getLastRound() == null){
                roundHeight = BigInteger.ZERO;
            }
            else {
                roundHeight = this.parentEpoch.getLastRound().roundHeight.add(BigInteger.ONE);
            }
        }

        sha3 = new DigestSHA3(256);
    }

    public Epoch getParentEpoch()
    {
        return(parentEpoch);
    }

    public List<Validator> getListValidators() { return(listValidators); }

    public BigInteger roundHeight(){return(roundHeight);}

    public void rebuildValidatorsList(String strRandomSource) {
        //pick max Util.VERIFIER_GROUP_SIZE from eligible list
        //not taken into account stake, rating and round r

        listValidators.clear();

        if (parentEpoch == null)
        {
            return;
        }

        if (parentEpoch.getEligibleList().size() <= Util.VERIFIER_GROUP_SIZE) {
            //plain copy and return
            for (int i = 0; i < parentEpoch.getEligibleList().size(); i++) {
                listValidators.add(new Validator(parentEpoch.getEligibleList().get(i)));
            }

            return;
        }

        //it's a kind of PoW :)
        int nonce = 1;
        int size = parentEpoch.getEligibleList().size();
        int startIdx = 0;
        while (listValidators.size() < Util.VERIFIER_GROUP_SIZE)
        {
            BigInteger bi = new BigInteger(sha3.digest((strRandomSource + Integer.toString(nonce)).getBytes()));

            startIdx = bi.mod(BigInteger.valueOf(size)).intValue();

            boolean flagFound = false;
            while (!flagFound)
            {
                Validator v = parentEpoch.getEligibleList().get(startIdx);

                if (listValidators.contains(v))
                {
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

}
