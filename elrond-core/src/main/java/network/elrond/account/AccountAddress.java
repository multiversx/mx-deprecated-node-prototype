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

    public PublicKey getPublicKey() {
        return new PublicKey(bytes);
    }

    public AccountAddress(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null");
        }
        this.bytes = bytes;
    }


    public static AccountAddress fromHexString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("value is not a HexaString!!!");
        }
        return new AccountAddress(Util.hexStringToByteArray(value));
    }

    public static AccountAddress fromBytes(byte[] value) {
        return new AccountAddress(value);
    }

    public static AccountAddress fromPublicKey(PublicKey key) {
        if (key == null) {
            throw new IllegalArgumentException("PublicKey cannot be null");
        }
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
        return "AccountAddress{" + Util.byteArrayToHexString(bytes) + '}';
    }
}
