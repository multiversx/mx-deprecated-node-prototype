package network.elrond.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

public class Util {
    public static final int VERIFIER_GROUP_SIZE = 21;
    public static String CRT_PUB_KEY = "0xA8";
    public static final int MAX_RATING = 10;
    //base cryptoeconomics:
    //  1 ERD  = 1000 mERDs (mili-ERDs)
    //  1 mERD = 1000 uERDs (micro-ERDs)
    //  1 uERD = 1000 nERDs (nano-ERDs)  = 100 sERDs (Satoshi-ERDs)
    //so 10 nERDs = 1 sERDs and 1 sERDs will be the minimum amount
    // 1 ERD = 100000000 sERDs OR 10^8 sERDs
    //all amounts are expressed in sERSs !!!
    public static final BigInteger MIN_STAKE = BigInteger.valueOf(10).pow(8);
    public static final float WEIGHT_STAKE_SPOS = 0.4f;
    public static final float WEIGHT_RATING_SPOS = 0.6f;
    public static final int MAX_LEN_ADDR = 42; //20 bytes x 2 chars + 0x
    public static final int MAX_LEN_PUB_KEY = 33;
    public static DigestSHA3 SHA3 = new DigestSHA3(256);
    public static SHA256.Digest SHA256 = new SHA256.Digest();

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }


    public static String getAddressFromPublicKey(byte[] pubKeyBytes) {
        if (pubKeyBytes == null) {
            return ("");
        }

        //step 1. get the data in the hexa form
        String strHexa = byteArrayToHexString(pubKeyBytes);

        if (strHexa.length() != Util.MAX_LEN_PUB_KEY * 2) {
            return ("");
        }

        //step 2. compute hash based on hexa form
        byte[] hash = SHA3.digest(strHexa.getBytes());

        if (hash.length != 32) {
            return ("");
        }

        //step 3. trim to last 20 bytes
        byte[] addr = Arrays.copyOfRange(hash, 12, 32);

        //step 4. convert to hexa form and add 0x
        return ("0x" + byteArrayToHexString(addr));
    }

    /**
     * Concatenates two byte arrays returning the resulting byte array
     *
     * @param first  the byte array that will be concatenated first
     * @param second the byte array that will be concatenated second
     * @return the concatenated byte array
     */
    public static byte[] concatenateArrays(byte[] first, byte[] second) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] result;
        try {
            output.write(first);
            output.write(second);
            result = output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }
}
