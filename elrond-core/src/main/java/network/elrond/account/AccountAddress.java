package network.elrond.account;

import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;

import java.io.Serializable;
import java.util.Arrays;

public class AccountAddress implements Serializable {
    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public AccountAddress(byte[] bytes) {
        this.bytes = bytes;
    }

    //JLS - NO!
//    public static AccountAddress fromString(String value) {
//        return new AccountAddress(value.getBytes());
//    }

    public static AccountAddress fromHexaString(String value) {
        String hexVal = value != null ? value.substring(2) : null;
        return new AccountAddress(Util.hexStringToByteArray(hexVal));
    }

    public static AccountAddress fromBytes(byte[] value) {
        return new AccountAddress(value);
    }

    public static AccountAddress fromPublicKey(PublicKey key) {
        byte[] buff = Util.getAddressFromPublicKeyAsByteArray(key.getQ().getEncoded(true));
        return fromBytes(buff);
    }

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

    public String toAddressString(){
        return("0x"+Util.byteArrayToHexString(bytes));
    }
}
