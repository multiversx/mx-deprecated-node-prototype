package network.elrond.account;

import network.elrond.core.Util;

import java.io.Serializable;
import java.util.Arrays;

public class AccountAddress implements Serializable {

    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }


    private AccountAddress(byte[] publicKeyBytes) {
        Util.check(publicKeyBytes != null,"publicKeyBytes != null" );
        this.bytes = publicKeyBytes;
    }

    public static AccountAddress fromHexString(String publicKeyHexString) {
        Util.check(publicKeyHexString != null, "publicKeyHexString!=null");
        Util.check(!publicKeyHexString.isEmpty(), "publicKeyHexString!=null");
        return new AccountAddress(Util.hexStringToByteArray(publicKeyHexString));
    }

    public static AccountAddress fromBytes(byte[] publicKeyBytes) {
        return new AccountAddress(publicKeyBytes);
    }

//    public static AccountAddress fromPublicKey(PublicKey key) {
//        if (key == null) {
//            throw new IllegalArgumentException("PublicKey cannot be null");
//        }
//        return fromBytes(key.getValue());
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountAddress that = (AccountAddress) o;
        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return "AccountAddress{" +
                "bytes=" + Arrays.toString(bytes) +
                "hex=" + Util.byteArrayToHexString(bytes) +
                '}';
    }
}
