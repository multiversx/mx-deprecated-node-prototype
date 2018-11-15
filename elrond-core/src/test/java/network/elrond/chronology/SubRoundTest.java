package network.elrond.chronology;

import static org.junit.Assert.*;

import org.junit.Test;

public class SubRoundTest {
    @Test
    public void printSubRound(){
        assertEquals(
        		"SubRound{RoundState=null, round=null, timestamp=0}", 
        		new SubRound(null, null, 0).toString());
        
        assertEquals(
        		"SubRound{RoundState=SubRoundType{START_ROUND, order=0, duration:0}, round=Round{index=0, start timestamp=0}, timestamp=123}", 
        		new SubRound(new Round(), RoundState.START_ROUND, 123).toString());
    }

}
