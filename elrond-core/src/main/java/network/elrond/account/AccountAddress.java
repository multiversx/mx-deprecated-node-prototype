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

    public PublicKey getPublicKey(){
        return  new PublicKey(bytes);
    }

    public AccountAddress(byte[] bytes) {
        this.bytes = bytes;
    }

    //JLS - NO!
//    public static AccountAddress fromString(String value) {
//        return new AccountAddress(value.getBytes());
//    }

    public static AccountAddress fromHexaString(String value) {
        if(value == null || value.isEmpty()){
            throw new IllegalArgumentException("value is not a HexaString!!!");
        }
        //String hexVal = value != null ? value.substring(2) : null;
        return new AccountAddress(Util.hexStringToByteArray(value));
    }

    public static AccountAddress fromBytes(byte[] value) {
        return new AccountAddress(value);
    }

    public static AccountAddress fromPublicKey(PublicKey key) {
        //String address = Util.getAddressFromPublicKey(key.getQ().getEncoded(true));
        return fromBytes(key.getValue());
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
}
