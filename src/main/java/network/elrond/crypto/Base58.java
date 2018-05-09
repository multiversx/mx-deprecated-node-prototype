package network.elrond.crypto;

import java.util.Arrays;

/**
 * Source adapted after:
 * https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/Base58.java
 */
public class Base58 {
    private static final char[] BASE58_CHARS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char ENCODED_ZERO = BASE58_CHARS[0];
    private static final int[] INDEXES = new int[128];

    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < BASE58_CHARS.length; i++) {
            INDEXES[BASE58_CHARS[i]] = i;
        }
    }

    /**
     * Base58 encoding function.
     * <p>
     * Transforms an array of bytes into an alphanumeric String (ASCII conversion is also done
     * to the characters in base58 alphabet)
     * that does not contain the characters that may look similar in different fonts: 0OIl.
     *
     * @param byteArray The byte array to be encoded
     * @return The String representation of the base58 encoding
     */
    public static String encode(byte[] byteArray) {
        if (byteArray.length == 0) {
            return "";
        }
        // Count leading zeros.
        int zeros = 0;
        while (zeros < byteArray.length && byteArray[zeros] == 0) {
            ++zeros;
        }
        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        byteArray = Arrays.copyOf(byteArray, byteArray.length); // since we modify it in-place
        char[] encoded = new char[byteArray.length * 2]; // upper bound
        int outputStart = encoded.length;
        for (int inputStart = zeros; inputStart < byteArray.length; ) {
            encoded[--outputStart] = BASE58_CHARS[divMode(byteArray, inputStart, 256, 58)];
            if (byteArray[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart;
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO;
        }
        // Return encoded string (including encoded leading zeros).
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

    /**
     * Base58 decoding function.
     * <p>
     * Transforms a {@link Base58} encoded string into a byte array
     *
     * @param str58 The string ({@link Base58} encoded) to be decoded
     * @return The byte array
     */
    public static byte[] decode(String str58) throws NumberFormatException {

        if (str58.length() == 0) {
            return new byte[0];
        }

        byte[] byteArray58 = new byte[str58.length()];
        for (int i = 0; i < byteArray58.length; i++) {
            char c = str58.charAt(i);
            int digit = (c < 128) ? INDEXES[c] : -1;
            if (digit < 0) {
                throw new NumberFormatException();
            }
        }

        int zeros = 0;
        // Count leading zeros.
        while (zeros < byteArray58.length && byteArray58[zeros] == 0) {
            ++zeros;
        }
        // Convert base-58 digits to base-256 digits.
        byte[] decoded = new byte[str58.length()];
        int outputStart = decoded.length;
        for (int inputStart = zeros; inputStart < byteArray58.length; ) {
            decoded[--outputStart] = divMode(byteArray58, inputStart, 58, 256);
            if (byteArray58[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Ignore extra leading zeroes that were added during the calculation.
        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            ++outputStart;
        }
        // Return decoded data (including original number of leading zeros).
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
    }

    /**
     * Convert a number from one base to another
     * <p>
     * Converts a number received as a byte array, with each element a "digit" in the
     * original base (fromBase), into a different base (toBase) number.
     *
     * @param number   byte array containing number to be converted
     * @param base Base of number before conversion
     * @param divisor   The number to divide by
     * @return The remainder
     */
    public static byte divMode(byte[] number, int startIndex, int base, int divisor) {
        // this is just long division which accounts for the base of the input digits
        int remainder = 0;
        for (int i = startIndex; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }

    /* For test */
    public static void main(String[] args) {
        byte[] nb = {120, 59, 57, 30, 22, 11, 98, 8};
        String result = Base58.encode(nb);

        System.out.println("Nb: " + nb + " in base 58: " + result);
        byte[] decoded = Base58.decode(result);
        System.out.println("decoded:" + result + " in base 256: " + decoded);
    }
}
