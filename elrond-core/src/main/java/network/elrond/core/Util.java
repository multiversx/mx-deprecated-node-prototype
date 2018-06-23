package network.elrond.core;

import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.jcajce.provider.digest.SHA256;
import org.spongycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

public class Util {
    private static final Logger logger = LogManager.getLogger(Util.class);

    public static final int VERIFIER_GROUP_SIZE = 21;
    public static String CRT_PUB_KEY = "0xA8";
    public static final int MAX_SCORE = 10;
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
    public static final int MAX_LEN_ADDR = 33; //equals public key
    public static final int MAX_LEN_PUB_KEY = 33;

    public static final ThreadLocal<DigestSHA3> SHA3 = ThreadLocal.withInitial(() -> new DigestSHA3(256));


    public static SHA256.Digest SHA256 = new SHA256.Digest();

    public static BigInteger BIG_INT_MIN_ONE = BigInteger.valueOf(-1);

    public static byte[] EMPTY_BYTE_ARRAY;
    public static byte[] EMPTY_DATA_HASH;

    public static final PrivateKey PRIVATE_KEY_MINTING;
    public static final PublicKey PUBLIC_KEY_MINTING;
    public static final BigInteger VALUE_MINTING;

    public static final String TEST_ADDRESS = "0326e7875aadaba270ae93ec40ef4706934d070eb21c9acad4743e31289fa4ebc7";


    static {
        EMPTY_BYTE_ARRAY = new byte[0];
        EMPTY_DATA_HASH = SHA3.get().digest(EMPTY_BYTE_ARRAY);

        PRIVATE_KEY_MINTING = new PrivateKey("MINTING ADDRESS FOR INITIAL TRANSFER");
        PUBLIC_KEY_MINTING = new PublicKey(PRIVATE_KEY_MINTING);

        //21 milion ERDs
        VALUE_MINTING = BigInteger.TEN.pow(14).multiply(BigInteger.valueOf(21));
    }

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

        //step 1. getAccountState the data in the hexa form
        String strHexa = byteArrayToHexString(pubKeyBytes);

        if (strHexa.length() != Util.MAX_LEN_PUB_KEY * 2) {
            return ("");
        }

        return strHexa;

//        //step 2. compute hash based on hexa form
//        byte[] hash = SHA3.get().digest(strHexa.getBytes());
//
//        if (hash.length != 32) {
//            return ("");
//        }
//
//        //step 3. trim to last 20 bytes
//        byte[] addr = Arrays.copyOfRange(hash, 12, 32);
//
//        //step 4. convert to hexa form and add 0x
//        return ("0x" + byteArrayToHexString(addr));
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


    public static String getHashEncoded64(String data) {
        byte[] buff = SHA3.get().digest(data.getBytes());

        return (new String(Base64.encode(buff)));
    }

    public static String getDataEncoded64(byte[] data) {
        return (new String(Base64.encode(data)));
    }

    /**
     * Throws an IllegalArgumentException in case boolean expression is not true
     *
     * @param test    boolean expression
     * @param message string message to be given as argument to the exception in case of failure
     */
    public static void check(boolean test, String message) {
        if (!test) {
            IllegalArgumentException ex = new IllegalArgumentException(message);
            logger.throwing(ex);
            throw ex;
        }
    }

    public static void deleteDirectory(File dir) throws IOException {
        logger.traceEntry("params: {}", dir);
        try {
            FileUtils.deleteDirectory(dir);
            logger.trace("done");
        } catch (IOException ex) {
            logger.throwing(ex);
            throw ex;
        }
        logger.traceExit();
    }
}
