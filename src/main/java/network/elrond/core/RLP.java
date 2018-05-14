package network.elrond.core;

import java.math.BigInteger;

/**
 * The RLP class implements Recursive Length Prefix encoding/decoding model.
 * Based on Ethereum Team work!
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public class RLP {
    public static final int ONE_ITEM_THRESHOLD = 0x80;


    public static byte[] encode(Object objData) {
        if (objData instanceof Integer)
        {
            int data = (Integer)objData;

            if (data < 0) {

            }

            if (data < ONE_ITEM_THRESHOLD){

            }
        }

        return(null);
    }



}
