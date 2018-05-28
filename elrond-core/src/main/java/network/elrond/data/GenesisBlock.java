package network.elrond.data;

import network.elrond.service.AppServiceProvider;

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
        listPubKeys = new ArrayList<String>();
        prevBlockHash = STR_GENESIS_BLOCK.getBytes();
        listTXHashes = new ArrayList<byte[]>();
//        //TO DO
//
//        txMint = new Transaction();
//        txMint.setNonce(BigInteger.ZERO);
//        txMint.setPubKey(STR_GENESIS_BLOCK);
//        txMint.setRecvAddress("0xbde7dc0e2128f49b1b1b1808cbd1ee42605d07fe");
//        txMint.setSendAddress("0x0000000000000000000000000000000000000000");
//        //50 mil ERDs
//        txMint.setValue(BigInteger.TEN.pow(14).multiply(BigInteger.valueOf(50)));
//
//        this.listTXHashes.add(AppServiceProvider.getTransactionService().getHash(txMint, true));



        //add here a minting address
        shard = 0;
        appStateHash = STR_GENESIS_BLOCK.getBytes();
        //TO DO
        //compute an initial state hash
    }

}
