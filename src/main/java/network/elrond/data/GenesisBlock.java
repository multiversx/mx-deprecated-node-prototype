package network.elrond.data;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * The GenesisBlock class extends Block and has HW fields
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public class GenesisBlock extends Block{
    public static String STR_GENESIS_BLOCK = "GENESIS";

    /**
     * Implicit constructor
     */
    public GenesisBlock() {
        this.nonce = BigInteger.ZERO;
        hashNoSig = new byte[0];
        hash = new byte[0];
        listPubKeys = new ArrayList<String>();
        prevBlockHash = STR_GENESIS_BLOCK.getBytes();
        listTXHashes = new ArrayList<byte[]>();
        //TO DO
        //add here a minting address
        shard = 0;
        appStateHash = STR_GENESIS_BLOCK.getBytes();
        //TO DO
        //compute an initial state hash
    }
}
