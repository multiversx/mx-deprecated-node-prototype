package network.elrond.account;

import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Arrays;

public class AccountAddress implements Serializable {

    private static final Logger logger = LogManager.getLogger(AccountAddress.class);

    public static final AccountAddress EMPTY_ADDRESS = new AccountAddress(new byte[0]);


    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }


    private AccountAddress(byte[] publicKeyBytes) {
        logger.traceEntry("params: {}", publicKeyBytes);
        Util.check(publicKeyBytes != null, "publicKeyBytes != null");
        this.bytes = publicKeyBytes;
        logger.traceExit();
    }

    public static AccountAddress fromHexString(String publicKeyHexString) {
        logger.traceEntry("params: {}", publicKeyHexString);
        Util.check(publicKeyHexString != null, "publicKeyHexString!=null");
        Util.check(!publicKeyHexString.isEmpty(), "publicKeyHexString!=null");
        return logger.traceExit(new AccountAddress(Util.hexStringToByteArray(publicKeyHexString)));
    }

    public static AccountAddress fromBytes(byte[] publicKeyBytes) {
        return new AccountAddress(publicKeyBytes);
    }

    @Override
    public boolean equals(Object o) {
        logger.traceEntry("params: this>{} {}", this, o);
        if (this == o) {
            logger.trace("same object");
            return logger.traceExit(true);
        }

        if (o == null || getClass() != o.getClass()) {
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
