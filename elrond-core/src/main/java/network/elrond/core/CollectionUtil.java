package network.elrond.core;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtil {

    public static <E> int size(Collection<E> collection) {
        return collection != null ? collection.size() : 0;
    }


    public static <E> String implode(List<E> list, String glue) {
        return implode(list, glue, e -> e);
    }

    public static <E, F> String implode(List<E> list, String glue, Function<E, F> function) {

        List<F> elems = list.stream().map(function).collect(Collectors.toList());

        String result = "";
        for (int i = 0; i < elems.size(); i++) {
            result += elems.get(i);
            if (i < list.size() - 1) {
                result += glue;
            }

        }

        return result;

    }

    public static <E> boolean isEmpty(Collection<E> collection) {
        return size(collection) == 0;
    }


    public static <E> boolean contains(List<E> list, E elem) {
        return list != null && list.contains(elem);
    }
}
