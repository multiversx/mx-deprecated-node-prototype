package network.elrond.core;

import java.math.BigInteger;
import java.util.Arrays;

public class ByteUtil {

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Creates a copy of bytes and appends b to the end of it
     */
    public static byte[] appendByte(byte[] bytes, byte b) {
        byte[] result = Arrays.copyOf(bytes, bytes.length + 1);
        result[result.length - 1] = b;
        return result;
    }

    /**
     * Returns the amount of nibbles that match each other from 0 ...
     * 	amount will never be larger than smallest input
     *
     * @param a - first input
     * @param b - second input
     * @return Number of bytes that match
     */
    public static int matchingNibbleLength(byte[] a, byte[] b) {
        int i = 0;
        int length = a.length < b.length ? a.length : b.length;
        while (i < length) {
            if (a[i] != b[i])
                break;
            i++;
        }
        return i;
    }

    /**
     * Cast hex encoded value from byte[] to int
     *
     * Limited to Integer.MAX_VALUE: 2^32-1 (4 bytes)
     *
     * @param b array contains the values
     * @return unsigned positive int value.
     */
    public static int byteArrayToInt(byte[] b) {
        if (b == null || b.length == 0)
            return 0;
        return new BigInteger(1, b).intValue();
    }
}
