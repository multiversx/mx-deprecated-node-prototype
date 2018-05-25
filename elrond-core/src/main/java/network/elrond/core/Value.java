package network.elrond.core;

import java.util.Arrays;
import java.util.List;

/**
 * Class to encapsulate an object and provide utilities for conversion
 */
public class Value {

    private Object value;

    public static Value fromRlpEncoded(byte[] data) {
        if (data != null && data.length != 0) {
            return new Value(RLP.decode(data, 0).getDecoded());
        } return null;
    }

    public Value(Object obj) {

        if (obj == null) return;

        if (obj instanceof Value) {
            this.value = ((Value) obj).asObj();
        } else {
            this.value = obj;
        }
    }

    /* *****************
     * 		Convert
     * *****************/

    public Object asObj() {
        return value;
    }

    public List<Object> asList() {
        Object[] valueArray = (Object[]) value;
        return Arrays.asList(valueArray);
    }

    public String asString() {
        if (isBytes()) {
            return new String((byte[]) value);
        } else if (isString()) {
            return (String) value;
        }
        return "";
    }

    public byte[] asBytes() {
        if(isBytes()) {
            return (byte[]) value;
        } else if(isString()) {
            return asString().getBytes();
        }
        return ByteUtil.EMPTY_BYTE_ARRAY;
    }

    public Value get(int index) {
        if(isList()) {
            // Guard for OutOfBounds
            if (asList().size() <= index) {
                return new Value(null);
            }
            if (index < 0) {
                throw new RuntimeException("Negative index not allowed");
            }
            return new Value(asList().get(index));
        }
        // If this wasn't a slice you probably shouldn't be using this function
        return new Value(null);
    }

    /* *****************
     * 		Utility
     * *****************/

    public byte[] encode() {
        return RLP.encode(value);
    }

    /* *****************
     * 		Checks
     * *****************/

    public boolean isList() {
        return value != null && value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive();
    }

    public boolean isString() {
        return value instanceof String;
    }

    public boolean isBytes() {
        return value instanceof byte[];
    }

    public boolean isHashCode(){
        return this.asBytes().length == 32;
    }

    public boolean isNull() {
        return value == null;
    }

    public int length() {
        if (isList()) {
            return asList().size();
        } else if (isBytes()) {
            return asBytes().length;
        } else if (isString()) {
            return asString().length();
        }
        return 0;
    }
}
