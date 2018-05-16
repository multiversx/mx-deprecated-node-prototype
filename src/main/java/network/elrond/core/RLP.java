package network.elrond.core;

import java.math.BigInteger;
import java.util.ArrayList;

import static java.util.Arrays.copyOfRange;
import static network.elrond.core.ByteUtil.byteArrayToInt;
import static org.bouncycastle.util.Arrays.concatenate;
import static org.bouncycastle.util.BigIntegers.asUnsignedByteArray;

import java.util.List;

/**
 * Recursive Length Prefix (RLP) encoding.
 *
 * The purpose of RLP is to encode arbitrarily nested arrays of binary data, and
 * RLP is the main encoding method used to serialize objects in Ethereum. The
 * only purpose of RLP is to encode structure; encoding specific atomic data
 * types (eg. strings, ints, floats) is left up to higher-order protocols; in
 * Ethereum the standard is that integers are represented in big endian binary
 * form. If one wishes to use RLP to encode a dictionary, the two suggested
 * canonical forms are to either use [[k1,v1],[k2,v2]...] with keys in
 * lexicographic order or to use the higher-level Patricia Tree encoding as
 * Ethereum does.
 *
 * The RLP encoding function takes in an item. An item is defined as follows:
 *
 * - A string (ie. byte array) is an item - A list of items is an item
 *
 * For example, an empty string is an item, as is the string containing the word
 * "cat", a list containing any number of strings, as well as more complex data
 * structures like ["cat",["puppy","cow"],"horse",[[]],"pig",[""],"sheep"]. Note
 * that in the context of the rest of this article, "string" will be used as a
 * synonym for "a certain number of bytes of binary data"; no special encodings
 * are used and no knowledge about the content of the strings is implied.
 *
 * See: https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-RLP
 *
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 01/04/2014 10:45
 *
 */

/**
 * The RLP class implements Recursive Length Prefix encoding/decoding model.
 * Based on Ethereum Team work!
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public class RLP {
/*
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
*/
    /** Allow for content up to size of 2^64 bytes **/
    private static double MAX_ITEM_LENGTH = Math.pow(256, 8);

    /**
     * Reason for threshold according to Vitalik Buterin:
     * 	- 56 bytes maximizes the benefit of both options
     * 	- if we went with 60 then we would have only had 4 slots for long strings
     * so RLP would not have been able to store objects above 4gb
     * 	- if we went with 48 then RLP would be fine for 2^128 space, but that's way too much
     * 	- so 56 and 2^64 space seems like the right place to put the cutoff
     * 	- also, that's where Bitcoin's varint does the cutof
     **/
    private static int SIZE_THRESHOLD = 56;

    /**
     * [0x80]
     * If a string is 0-55 bytes long, the RLP encoding consists of a single
     * byte with value 0x80 plus the length of the string followed by the
     * string. The range of the first byte is thus [0x80, 0xb7].
     */
    private static int OFFSET_SHORT_ITEM = 0x80;

    /**
     * [0xb7]
     * If a string is more than 55 bytes long, the RLP encoding consists of a
     * single byte with value 0xb7 plus the length of the length of the string
     * in binary form, followed by the length of the string, followed by the
     * string. For example, a length-1024 string would be encoded as
     * \xb9\x04\x00 followed by the string. The range of the first byte is thus
     * [0xb8, 0xbf].
     */
    private static int OFFSET_LONG_ITEM = 0xb7;

    /**
     * [0xc0]
     * If the total payload of a list (i.e. the combined length of all its
     * items) is 0-55 bytes long, the RLP encoding consists of a single byte
     * with value 0xc0 plus the length of the list followed by the concatenation
     * of the RLP encodings of the items. The range of the first byte is thus
     * [0xc0, 0xf7].
     */
    private static int OFFSET_SHORT_LIST = 0xc0;

    /**
     * [0xf7]
     * If the total payload of a list is more than 55 bytes long, the RLP
     * encoding consists of a single byte with value 0xf7 plus the length of the
     * length of the list in binary form, followed by the length of the list,
     * followed by the concatenation of the RLP encodings of the items. The
     * range of the first byte is thus [0xf8, 0xff].
     */
    private static int OFFSET_LONG_LIST = 0xf7;

    /* ******************************************************
     * 						DECODING						*
     * ******************************************************/

    /**
     * Reads any RLP encoded byte-array and returns all objects as byte-array or list of byte-arrays
     *
     * @param data RLP encoded byte-array
     * @param pos position in the array to start reading
     * @return DecodeResult encapsulates the decoded items as a single Object and the final read position
     */
    public static DecodeResult decode(byte[] data, int pos) {
        if (data == null || data.length < 1) {
            return null;
        }
        int prefix = data[pos] & 0xFF;
        if (prefix == OFFSET_SHORT_ITEM) {
            return new DecodeResult(pos+1, ""); // means no length or 0
        } else if (prefix < OFFSET_SHORT_ITEM) {
            return new DecodeResult(pos+1, new byte[] { data[pos] }); // byte is its own RLP encoding
        } else if (prefix < OFFSET_LONG_ITEM) {
            int len = prefix - OFFSET_SHORT_ITEM; // length of the encoded bytes
            return new DecodeResult(pos+1+len, copyOfRange(data, pos+1, pos+1+len));
        } else if (prefix < OFFSET_SHORT_LIST) {
            int lenlen = prefix - OFFSET_LONG_ITEM; // length of length the encoded bytes
            int lenbytes = byteArrayToInt(copyOfRange(data, pos+1, pos+1+lenlen)); // length of encoded bytes
            return new DecodeResult(pos+1+lenlen+lenbytes, copyOfRange(data, pos+1+lenlen, pos+1+lenlen+lenbytes));
        } else if (prefix <= OFFSET_LONG_LIST) {
            int len = prefix - OFFSET_SHORT_LIST; // length of the encoded list
            int prevPos = pos; pos++;
            return decodeList(data, pos, prevPos, len);
        } else if (prefix < 0xFF) {
            int lenlen = prefix - OFFSET_LONG_LIST; // length of length the encoded list
            int lenlist = byteArrayToInt(copyOfRange(data, pos+1, pos+1+lenlen)); // length of encoded bytes
            pos = pos + lenlen + 1; // start at position of first element in list
            int prevPos = lenlist;
            return decodeList(data, pos, prevPos, lenlist);
        } else {
            throw new RuntimeException("Only byte values between 0x00 and 0xFF are supported, but got: " + prefix);
        }
    }

    private static DecodeResult decodeList(byte[] data, int pos, int prevPos, int len) {
        List<Object> slice = new ArrayList<>();
        for (int i = 0; i < len;) {
            // Get the next item in the data list and append it
            DecodeResult result = decode(data, pos);
            slice.add(result.getDecoded());
            // Increment pos by the amount bytes in the previous read
            prevPos = result.getPos();
            i += (prevPos - pos);
            pos = prevPos;
        }
        return new DecodeResult(pos, slice.toArray());
    }

    /* ******************************************************
     * 						ENCODING						*
     * ******************************************************/

    /**
     * Turn Object into its RLP encoded equivalent of a byte-array
     * Support for String, Integer, BigInteger and Lists of any of these types.
     *
     * @param input as object or List of objects
     * @return byte[] RLP encoded
     */
    public static byte[] encode(Object input) {
        Value val = new Value(input);
        if (val.isList()) {
            List<Object> inputArray = val.asList();
            if (inputArray.size() == 0) {
                return encodeLength(inputArray.size(), OFFSET_SHORT_LIST);
            }
            byte[] output = ByteUtil.EMPTY_BYTE_ARRAY;
            for (Object object : inputArray) {
                output = concatenate(output, encode(object));
            }
            byte[] prefix = encodeLength(output.length, OFFSET_SHORT_LIST);
            return concatenate(prefix, output);
        } else {
            byte[] inputAsBytes = toBytes(input);
            if (inputAsBytes.length == 1) {
                return inputAsBytes;
            } else {
                byte[] firstByte = encodeLength(inputAsBytes.length, OFFSET_SHORT_ITEM);
                return concatenate(firstByte, inputAsBytes);
            }
        }
    }

    /** Integer limitation goes up to 2^31-1 so length can never be bigger than MAX_ITEM_LENGTH */
    public static byte[] encodeLength(int length, int offset) {
        if (length < SIZE_THRESHOLD) {
            byte firstByte = (byte) (length + offset);
            return new byte[] { firstByte };
        } else if (length < MAX_ITEM_LENGTH) {
            byte[] binaryLength;
            if (length > 0xFF)
                binaryLength = BigInteger.valueOf(length).toByteArray();
            else
                binaryLength = new byte[] { (byte) length };
            byte firstByte = (byte) (binaryLength.length + offset + SIZE_THRESHOLD - 1);
            return concatenate(new byte[] { firstByte }, binaryLength);
        } else {
            throw new RuntimeException("Input too long");
        }
    }

    /*
     *	Utility function to convert Objects into byte arrays
     */
    private static byte[] toBytes(Object input) {
        if (input instanceof byte[]) {
            return (byte[]) input;
        } else if (input instanceof String) {
            String inputString = (String) input;
            return inputString.getBytes();
        } else if(input instanceof Long) {
            Long inputLong = (Long) input;
            return (inputLong == 0) ? ByteUtil.EMPTY_BYTE_ARRAY : asUnsignedByteArray(BigInteger.valueOf(inputLong));
        } else if(input instanceof Integer) {
            Integer inputInt = (Integer) input;
            return (inputInt == 0) ? ByteUtil.EMPTY_BYTE_ARRAY : asUnsignedByteArray(BigInteger.valueOf(inputInt.intValue()));
        } else if(input instanceof BigInteger) {
            BigInteger inputBigInt = (BigInteger) input;
            return (inputBigInt == BigInteger.ZERO) ? ByteUtil.EMPTY_BYTE_ARRAY : asUnsignedByteArray(inputBigInt);
        } else if (input instanceof Value) {
            Value val = (Value) input;
            return toBytes(val.asObj());
        }
        throw new RuntimeException("Unsupported type: Only accepting String, Integer and BigInteger for now");
    }
}
