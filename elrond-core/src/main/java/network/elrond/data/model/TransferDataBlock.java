package network.elrond.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransferDataBlock<D> implements Serializable {
    private final List<D> dataList;
    private final String referenceBlockHash;

    public TransferDataBlock(String referenceBlockHash) {
        dataList = new ArrayList<>();
        this.referenceBlockHash = referenceBlockHash;
    }

    public List<D> getDataList() {
        return dataList;
    }

    public String getHash() {
        return referenceBlockHash;
    }
}
