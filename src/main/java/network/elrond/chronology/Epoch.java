package network.elrond.chronology;

import network.elrond.consensus.Validator;

import java.util.ArrayList;
import java.util.List;

public class Epoch {
    private static List<Validator> listWaiting;
    private static List<Validator> listEligible;

    static{
        listWaiting = new ArrayList<Validator>();
        listEligible = new ArrayList<Validator>();
    }
}
