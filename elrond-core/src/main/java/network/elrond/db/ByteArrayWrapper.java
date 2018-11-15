package network.elrond.db;

import java.util.Arrays;

import network.elrond.core.FastByteComparisons;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * Created on: 11/06/2014 15:02
 */
public class ByteArrayWrapper implements Comparable<ByteArrayWrapper> {

    private static final Logger logger = LogManager.getLogger(ByteArrayWrapper.class);

    private final byte[] data;

    public ByteArrayWrapper(byte[] data) {
        logger.traceEntry("params: {}", data);

        Util.check(data != null, "data is null");

        this.data = data;

        logger.traceExit();
    }

    @Override
	public boolean equals(Object other) {
        logger.traceEntry("params: {}", other);

        if (!(other instanceof ByteArrayWrapper)) {
            logger.trace("Not same class!");
            return logger.traceExit(false);
        }

        byte[] otherData = ((ByteArrayWrapper) other).getData();
        return logger.traceExit(FastByteComparisons.compareTo(
                data, 0, data.length,
                otherData, 0, otherData.length) == 0);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public int compareTo(ByteArrayWrapper o) {
        return FastByteComparisons.compareTo(
                data, 0, data.length,
                o.getData(), 0, o.getData().length);
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return Hex.toHexString(data);
    }
}