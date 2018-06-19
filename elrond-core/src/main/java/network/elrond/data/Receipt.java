package network.elrond.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Receipt implements Serializable {

    private String blockHash;
    private String transactionHash;
    private ReceiptStatus status;
    private List<String> logs;

    private Receipt() {

    }

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

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public void setStatus(ReceiptStatus status) {
        this.status = status;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "blockHash='" + blockHash + '\'' +
                ", transactionHash='" + transactionHash + '\'' +
                ", status=" + status +
                ", logs=" + logs +
                '}';
    }
}
