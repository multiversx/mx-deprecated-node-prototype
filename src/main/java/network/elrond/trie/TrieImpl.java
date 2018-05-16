package network.elrond.trie;
import network.elrond.core.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.spongycastle.util.encoders.Hex;

//public class TrieImpl implements Trie {
public class TrieImpl {
/*
    private static final Logger logger = LoggerFactory.getLogger("trie");

    private Object prevRoot;
    private Object root;


    public TrieImpl(Object root) { // PMS
        this.root = root;
        this.prevRoot = root;
    }

    @Override
    public byte[] get(byte[] key) {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving key {}", Hex.toHexString(key));
        byte[] k = binToNibbles(key);
        Value c = new Value(this.get(this.root, k));

        return (c == null)? null : c.asBytes();
    }
*/

    /****************************************
     * 			Private functions			*
     ****************************************/
/*
    private Object get(Object node, byte[] key) {

        // Return the node if key is empty (= found)
        if (key.length == 0 || isEmptyNode(node)) {
            return node;
        }

        Value currentNode = this.getNode(node);
        if (currentNode == null) return null;

        if (currentNode.length() == PAIR_SIZE) {
            // Decode the key
            byte[] k = unpackToNibbles(currentNode.get(0).asBytes());
            Object v = currentNode.get(1).asObj();

            if (key.length >= k.length && Arrays.equals(k, copyOfRange(key, 0, k.length))) {
                return this.get(v, copyOfRange(key, k.length, key.length));
            } else {
                return "";
            }
        } else {
            return this.get(currentNode.get(key[0]).asObj(), copyOfRange(key, 1, key.length));
        }
    }
*/
}
