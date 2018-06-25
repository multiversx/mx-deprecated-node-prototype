package network.elrond.consensus;


public class ConsensusTest {
//    @Test
//    public void testPBFTBlock() {
//        PBFTBlock pbftb1 = new PBFTBlock();
//        ArrayList<Validator> arlValidators = new ArrayList<Validator>();
//
//        for (int i = 0; i < 21; i++) {
//            arlValidators.add(new Validator("0xA" + Integer.toString(i), "127.0.0.1",
//                    ConsensusAnswerType.NOT_ANSWERED));
//        }
//
//        Util.CRT_PUB_KEY = "0xA8";
//
//        //testing if listToTable has been loaded 2 times
//        pbftb1.setValidators(arlValidators);
//        TestCase.assertEquals(20, pbftb1.getListValidators().size());
//        pbftb1.setValidators(arlValidators);
//        TestCase.assertEquals(21, pbftb1.getListValidators().size());
//
//        //test if I can modify an answer
//        int cntValidator = 4;
//        pbftb1.setAnswerFromValidator(new Validator("0xA" + Integer.toString(cntValidator)),
//                ConsensusAnswerType.AGREE);
//        TestCase.assertEquals(ConsensusAnswerType.AGREE, pbftb1.getListValidators().getAccountState(cntValidator).getAnswer());
//
//        //test if the consensus changed crt validator answer
//        cntValidator = 8;
//        pbftb1.validate();
//        TestCase.assertEquals(ConsensusAnswerType.AGREE, pbftb1.getListValidators().getAccountState(cntValidator).getAnswer());
//
//        //test consensus
//        //only 2 agreed
//        TestCase.assertEquals(ConsensusAnswerType.DISAGREE, pbftb1.getStatusPBFT());
//        //testing the pbft status if only the first 14 validators agreed
//        for (int i = 0; i < 14; i++) {
//            pbftb1.setAnswerFromValidator(new Validator("0xA" + Integer.toString(i)),
//                    ConsensusAnswerType.AGREE);
//            TestCase.assertEquals(ConsensusAnswerType.DISAGREE, pbftb1.getStatusPBFT());
//        }
//        //testing if more than 15 validators aggred, the result should be agree
//        for (int i = 15; i < 21; i++) {
//            pbftb1.setAnswerFromValidator(new Validator("0xA" + Integer.toString(i)),
//                    ConsensusAnswerType.AGREE);
//            TestCase.assertEquals(ConsensusAnswerType.AGREE, pbftb1.getStatusPBFT());
//        }
//
//    }
//
//    @Test
//    public void testRoundSPoS() {
//        Epoch e = new Epoch();
//
//        //test round r
//        e.createRound();
//        e.createRound();
//        Round r = e.createRound();
//        TestCase.assertEquals(BigInteger.valueOf(2), r.roundIndex());
//
//        //test for eligible listToTable size = 3 => plain copy
//        for (int i = 0; i < 3; i++) {
//            e.getEligibleList().add(new Validator("0xA" + Integer.toString(i)));
//        }
//
//        r.rebuildValidatorsList("RANDOM_SOURCE_1");
//
//        System.out.printf("1. Original epoch listToTable [%d]:", e.getEligibleList().size());
//        System.out.println();
//        System.out.println("==========================================================");
//        displayListValidators(e.getEligibleList());
//
//        System.out.printf("1. Validators [%d]:", r.getListValidators().size());
//        System.out.println();
//        System.out.println("==========================================================");
//        displayListValidators(r.getListValidators());
//
//        //test for eligible listToTable size = 100 => random chosen
//        for (int i = 0; i < 100; i++) {
//            e.getEligibleList().add(new Validator("0xA" + Integer.toString(i)));
//        }
//
//        r.rebuildValidatorsList("RANDOM_SOURCE_2");
//
//        System.out.printf("2. Original epoch listToTable [%d]:", e.getEligibleList().size());
//        System.out.println();
//        System.out.println("==========================================================");
//        displayListValidators(e.getEligibleList());
//
//        System.out.printf("2. Validators [%d]:", r.getListValidators().size());
//        System.out.println();
//        System.out.println("==========================================================");
//        displayListValidators(r.getListValidators());
//
//    }
//
//    @Test
//    public void testGenerateValidatorList() {
//        Epoch e = new Epoch();
//
//        List<Validator> vList = e.getEligibleList();
//
//        vList.add(new Validator("0xA01", "", ConsensusAnswerType.NOT_ANSWERED, BigInteger.valueOf(0), 0));
//        vList.add(new Validator("0xA02", "", ConsensusAnswerType.NOT_ANSWERED, BigInteger.valueOf(1), 0));
//        vList.add(new Validator("0xA03", "", ConsensusAnswerType.NOT_ANSWERED, BigInteger.valueOf(0), 1));
//        vList.add(new Validator("0xA04", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE, 0));
//        vList.add(new Validator("0xA05", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE, 1));
//        vList.add(new Validator("0xA06", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE.multiply(BigInteger.valueOf(2)), 0));
//        vList.add(new Validator("0xA07", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE.multiply(BigInteger.valueOf(2)), 1));
//        vList.add(new Validator("0xA08", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE.multiply(BigInteger.valueOf(2)), 2));
//        vList.add(new Validator("0xA09", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE.multiply(BigInteger.valueOf(10)), 2));
//        vList.add(new Validator("0xA10", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE.multiply(BigInteger.valueOf(10)), 10));
//        vList.add(new Validator("0xA11", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE.multiply(BigInteger.valueOf(10)), -1));
//        vList.add(new Validator("0xA12", "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE.subtract(BigInteger.valueOf(1)), 0));
//
//        Round r = e.createRound();
//
//        List<Validator> lResult = r.generateWeightedEligibleList();
//
//        //plain copy test
//        TestCase.assertEquals(7, lResult.size());
//
//        for (int i = 20; i < 41; i++)
//        {
//            vList.add(new Validator("0xA" + Integer.toString(i), "", ConsensusAnswerType.NOT_ANSWERED, Util.MIN_STAKE, 0));
//        }
//
//        lResult = r.generateWeightedEligibleList();
//
//
//        System.out.printf("1. Original epoch listToTable [%d]:", e.getEligibleList().size());
//        System.out.println();
//        System.out.println("==========================================================");
//        displayListValidators(e.getEligibleList());
//
//        System.out.printf("1. Validators [%d]:", lResult.size());
//        System.out.println();
//        System.out.println("==========================================================");
//        displayListValidators(lResult);
//
//
//    }
//
//    private void displayListValidators(List<Validator> listToTable) {
//        for (int i = 0; i < listToTable.size(); i++) {
//            Validator v = listToTable.getAccountState(i);
//
//            System.out.println(v.getPubKey() + ", S: " + v.getStake().toString(10) + ", R: " + v.getRating());
//        }
//        System.out.println();
//    }

}
