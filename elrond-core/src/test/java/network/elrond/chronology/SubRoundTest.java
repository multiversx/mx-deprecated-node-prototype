package network.elrond.chronology;

import org.junit.Test;

public class SubRoundTest {
    @Test
    public void printSubRound(){
        System.out.println(new SubRound());

        SubRound subRound = new SubRound();
        subRound.setRoundState(RoundState.START_ROUND);
        subRound.setRound(new Round());

        System.out.println(subRound);

    }

}
