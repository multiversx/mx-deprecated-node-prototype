
import junit.framework.TestCase;
import network.elrond.chronology.Epoch;
import network.elrond.chronology.Round;
import org.junit.Test;
import network.elrond.consensus.*;
import network.elrond.chronology.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ConsensusTest {
    @Test
    public void testPBFTBlock() {
        PBFTBlock pbftb1 = new PBFTBlock();
        ArrayList<Validator> arlValidators = new ArrayList<Validator>();

        for (int i = 0; i < 21; i++)
        {
            arlValidators.add(new Validator("0xA" + Integer.toString(i), "127.0.0.1",
                    ConsensusAnswerType.NOT_ANSWERED));
        }

        ConsensusUtil.CRT_PUB_KEY = "0xA8";

        //testing if list has been loaded 2 times
        pbftb1.setValidators(arlValidators);
        TestCase.assertEquals(21, pbftb1.getListValidators().size());
        pbftb1.setValidators(arlValidators);
        TestCase.assertEquals(21, pbftb1.getListValidators().size());

        //test if I can modify an answer
        int cntValidator = 4;
        pbftb1.getAnswerFromValidator(new Validator("0xA" + Integer.toString(cntValidator)),
                ConsensusAnswerType.AGREE);
        TestCase.assertEquals(ConsensusAnswerType.AGREE, pbftb1.getListValidators().get(cntValidator).getAnswer());

        //test if the consensus changed crt validator answer
        cntValidator = 8;
        pbftb1.validate();
        TestCase.assertEquals(ConsensusAnswerType.AGREE, pbftb1.getListValidators().get(cntValidator).getAnswer());

        //test consensus
        //only 2 agreed
        TestCase.assertEquals(ConsensusAnswerType.DISAGREE, pbftb1.getStatusPBFT());
        //testing the pbft status if only the first 14 validators agreed
        for (int i = 0; i < 14; i++)
        {
            pbftb1.getAnswerFromValidator(new Validator("0xA" + Integer.toString(i)),
                    ConsensusAnswerType.AGREE);
            TestCase.assertEquals(ConsensusAnswerType.DISAGREE, pbftb1.getStatusPBFT());
        }
        //testing if more than 15 validators aggred, the result should be agree
        for (int i = 15; i < 21; i++)
        {
            pbftb1.getAnswerFromValidator(new Validator("0xA" + Integer.toString(i)),
                    ConsensusAnswerType.AGREE);
            TestCase.assertEquals(ConsensusAnswerType.AGREE, pbftb1.getStatusPBFT());
        }

    }

    @Test
    public void testRoundSPoS()
    {
        Epoch e = new Epoch();

        //test round r
        e.createRound();
        e.createRound();
        Round r = e.createRound();
        TestCase.assertEquals(BigInteger.valueOf(2), r.roundHeight());

        //test for eligible list size = 3 => plain copy
        for (int i = 0; i < 3; i++)
        {
            e.getEligibleList().add(new Validator("0xA" + Integer.toString(i)));
        }

        r.rebuildValidatorsList("RANDOM_SOURCE_1");

        System.out.printf("1. Original epoch list [%d]:", e.getEligibleList().size());
        System.out.println();
        System.out.println("==========================================================");
        displayListValidators(e.getEligibleList());

        System.out.printf("1. Validators [%d]:", r.getListValidators().size());
        System.out.println();
        System.out.println("==========================================================");
        displayListValidators(r.getListValidators());

        //test for eligible list size = 100 => random chosen
        for (int i = 0; i < 100; i++)
        {
            e.getEligibleList().add(new Validator("0xA" + Integer.toString(i)));
        }

        r.rebuildValidatorsList("RANDOM_SOURCE_2");

        System.out.printf("2. Original epoch list [%d]:", e.getEligibleList().size());
        System.out.println();
        System.out.println("==========================================================");
        displayListValidators(e.getEligibleList());

        System.out.printf("2. Validators [%d]:", r.getListValidators().size());
        System.out.println();
        System.out.println("==========================================================");
        displayListValidators(r.getListValidators());

    }

    private void displayListValidators(List<Validator> list) {
        for (int i = 0; i < list.size(); i++)
        {
            System.out.println(list.get(i).getPubKey());
        }
        System.out.println();
    }

}
