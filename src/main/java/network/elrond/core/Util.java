package network.elrond.core;

import java.security.MessageDigest;

public class Util {
    public static final int VERIFIER_GROUP_SIZE = 21;

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
}
