package network.elrond.account;

import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger logger = LogManager.getLogger(AccountAddress.class);

    public AccountAddress(byte[] bytes) {
        logger.traceEntry("params: {}", bytes);
        if (bytes == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Bytes cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.bytes = bytes;
        logger.traceExit();
    }


    public static AccountAddress fromHexString(String value) {
        logger.traceEntry("params: {}", value);
        if (value == null || value.isEmpty()) {
            IllegalArgumentException ex = new IllegalArgumentException("value is not a HexaString!!!");
            logger.throwing(ex);
            throw ex;
        }

        return logger.traceExit(new AccountAddress(Util.hexStringToByteArray(value)));
    }

    public static AccountAddress fromBytes(byte[] value) {
        return new AccountAddress(value);
    }

    public static AccountAddress fromPublicKey(PublicKey key) {
        logger.traceEntry();
        if (key == null) {
            IllegalArgumentException ex = new IllegalArgumentException("PublicKey cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        logger.trace("Public key: {}", key.getValue());

        return logger.traceExit(fromBytes(key.getValue()));
    }

    @Override
    public boolean equals(Object o) {
        logger.traceEntry("params: this>{} {}", this, o);
        if (this == o){
            logger.trace("same object");
            return logger.traceExit(true);
        }

        if (o == null || getClass() != o.getClass()){
            logger.trace("object null, class not the same");
            return logger.traceExit(false);
        }
        AccountAddress that = (AccountAddress) o;
        return logger.traceExit(Arrays.equals(bytes, that.bytes));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return String.format("AccountAddress{%s}", Util.byteArrayToHexString(bytes));
    }
}
