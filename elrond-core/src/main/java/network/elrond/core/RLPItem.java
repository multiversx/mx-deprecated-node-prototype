package network.elrond.core;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 21/04/14 16:26
 */
public class RLPItem implements RLPElement {

    byte[] rlpData;

    public RLPItem(byte[] rlpData) {
        this.rlpData = rlpData;
    }

    @Override
	public byte[] getRLPData() {
        if (rlpData.length == 0)
            return null;
        return rlpData;
    }
}
