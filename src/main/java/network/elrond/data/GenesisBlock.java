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
    /**
     * Implicit constructor
     */
    public GenesisBlock() {
        this.nonce = BigInteger.ZERO;
        hashNoSig = new byte[0];
        hash = new byte[0];
        listPubKeys = new ArrayList<String>();
        prevBlockHash = ("GENESIS").getBytes();
        listTXHashes = new ArrayList<byte[]>();
        //TO DO
        //add here a minting address
        shard = 0;
        appStateHash = ("GENESIS").getBytes();
        //TO DO
        //compute an initial state hash
    }
}
