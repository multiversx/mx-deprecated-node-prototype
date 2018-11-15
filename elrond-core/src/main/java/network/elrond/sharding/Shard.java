package network.elrond.sharding;

import java.io.Serializable;
import java.util.Objects;

public class Shard implements Serializable {
    private Integer index = -1;

    public Shard(){
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

}
