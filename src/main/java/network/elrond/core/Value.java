package network.elrond.core;

/**
 * Class to encapsulate an object and provide utilities for conversion
 */
public class Value {

    private Object value;

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
}
