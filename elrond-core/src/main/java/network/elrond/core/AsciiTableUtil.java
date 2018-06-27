package network.elrond.core;

import network.elrond.AsciiTable;
import network.elrond.data.AsciiPrintable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsciiTableUtil {


    public static String listToTables(List<? extends AsciiPrintable> tables) {

        StringBuilder builder = new StringBuilder();
        for (AsciiPrintable table : tables) {
            builder.append(table.print().render());
        }
        return builder.toString();

    }

    public static <E> AsciiTable listToTable(String title, List<E> list) {
        return listToTable(title, list, e -> e);
    }

    public static <E, F> AsciiTable listToTable(String title, List<E> list, Function<E, F> function) {

        List<F> elems = list.stream().map(function).collect(Collectors.toList());

        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(30);

        table.getColumns().add(new AsciiTable.Column(title));

        for (F elem : elems) {
            AsciiTable.Row row0 = new AsciiTable.Row();
            row0.getValues().add(elem.toString());
            table.getData().add(row0);
        }


        table.calculateColumnWidth();
        return table;


    }

}
