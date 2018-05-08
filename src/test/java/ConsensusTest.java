
import junit.framework.TestCase;
import org.junit.Test;
import network.elrond.consensus.*;

import java.util.ArrayList;

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
        pbftb1.runConsensus();
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



}
