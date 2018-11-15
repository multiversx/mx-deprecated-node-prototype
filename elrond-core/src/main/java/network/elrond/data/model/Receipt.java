package network.elrond.data.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Receipt implements Serializable {

    private String blockHash;
    private final String transactionHash;
    private final ReceiptStatus status;
    private final List<String> logs;

    public Receipt(
            String transactionHash,
            ReceiptStatus status,
            String... logs) {

        this(null, transactionHash, status, logs);
    }

    public Receipt(
            String blockHash,
            String transactionHash,
            ReceiptStatus status,
            String... logs) {

        this.blockHash = blockHash;
        this.transactionHash = transactionHash;
        this.status = status;
        this.logs = Arrays.asList(logs);
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public ReceiptStatus getStatus() {
        return status;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public String toString() {
        return String.format("Receipt{blockHash='%s', transactionHash='%s', status=%s, logs='%s'}",
            blockHash, transactionHash, status, logs);
    }
}
