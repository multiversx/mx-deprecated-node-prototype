package network.elrond.core;

import java.util.ArrayList;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 21/04/14 16:26
 */
public class RLPList extends ArrayList<RLPElement> implements RLPElement {

    byte[] rlpData;

    public void setRLPData(byte[] rlpData) {
        this.rlpData = rlpData;
    }

    @Override
	public byte[] getRLPData() {
        return rlpData;
    }
}
