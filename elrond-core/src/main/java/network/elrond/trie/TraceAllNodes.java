package network.elrond.trie;

import network.elrond.core.Value;
import network.elrond.crypto.util.encoders.Hex;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 29/08/2014 10:46
 */

public class TraceAllNodes implements TrieImpl.ScanAction {

    StringBuilder output = new StringBuilder();

    @Override
    public void doOnNode(byte[] hash, Value node) {

        output.append(Hex.toHexString(hash)).append(" ==> ").append(node.toString()).append("\n");
    }

    public String getOutput() {
        return output.toString();
    }
}