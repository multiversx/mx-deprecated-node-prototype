package network.elrond.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransferDataBlock<D> implements Serializable {
    private List<D> dataList;
    private String referenceBlockHash;

    public TransferDataBlock() {
        dataList = new ArrayList<>();
    }

    public List<D> getDataList() {
        return dataList;
    }

    public String getHash() {
        return referenceBlockHash;
    }

    public void setHash(String hash) {
        referenceBlockHash = hash;
    }
}
