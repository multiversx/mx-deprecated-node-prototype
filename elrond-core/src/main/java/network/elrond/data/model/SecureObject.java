package network.elrond.data.model;

import network.elrond.core.Util;
import network.elrond.crypto.Signature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public class SecureObject<T> implements Serializable {
    private static final Logger logger = LogManager.getLogger(SecureObject.class);

    private final T object;
    private final Signature signature;
    private final byte[] publicKey;

    public SecureObject(T object, Signature signature, byte[] publicKey) {
        logger.traceEntry("params: {} {} {}", object, signature, publicKey);
        Util.check(object != null, "object != null");
        Util.check(signature != null, "signature != null");
        Util.check(publicKey != null, "publicKey != null");

        this.object = object;
        this.signature = signature;
        this.publicKey = publicKey;
        logger.traceExit();
    }

    public T getObject() {
        return object;
    }

    public Signature getSignature() {
        return signature;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
