package network.elrond.core;

import java.util.Collection;

public class CollectionUtil {

    public static <E> int size(Collection<E> collection) {
        return collection != null ? collection.size() : 0;
    }

}
