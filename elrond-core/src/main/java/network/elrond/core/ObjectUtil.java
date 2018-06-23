package network.elrond.core;

public class ObjectUtil {

    public static <E> boolean isEqual(E o1, E o2) {
        return o1 != null && o1.equals(o2);
    }

}
