package network.elrond.sharding;

import network.elrond.AsciiTable;
import network.elrond.data.AsciiPrintable;

import java.io.Serializable;
import java.util.Objects;

public class Shard implements Serializable, AsciiPrintable {
    private Integer index = -1;

    private Shard() {

    }

    public Shard(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Shard{" + "index=" + index + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shard shard = (Shard) o;
        return Objects.equals(index, shard.index);
    }

    @Override
    public int hashCode() {

        return Objects.hash(index);
    }

    @Override
    public AsciiTable print() {

        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(30);

        table.getColumns().add(new AsciiTable.Column("Shard"));

        AsciiTable.Row row1 = new AsciiTable.Row();
        row1.getValues().add("Index");
        row1.getValues().add(index + "");
        table.getData().add(row1);

        table.calculateColumnWidth();
        return table;
    }
}
