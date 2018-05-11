package network.elrond.core;

import java.math.BigInteger;
import java.security.MessageDigest;
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
    public static final int MAX_LEN_PUB_KEY = 66; //33 bytes x 2 chars

    public static DigestSHA3 SHA3;

    static{
        SHA3 = new DigestSHA3(256);
    }

    //JLS: oare nu e mai bine sa lucram cu hashuri pe array de bytes? Vom face economie la ceea ce transmitem pe fir
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder(); // This will contain hash as hexidecimal

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
