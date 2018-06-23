package network.elrond.benchmark;

public class MultipleTransactionResult {
    private Integer SuccessfulTransactionsNumber = 0;
    private Integer FailedTransactionsNumber = 0;

    public Integer getSuccessfulTransactionsNumber() {
        return SuccessfulTransactionsNumber;
    }

    public void setSuccessfulTransactionsNumber(Integer successfulTransactionsNumber) {
        SuccessfulTransactionsNumber = successfulTransactionsNumber;
    }

    public Integer getFailedTransactionsNumber() {
        return FailedTransactionsNumber;
    }

    public void setFailedTransactionsNumber(Integer failedTransactionsNumber) {
        FailedTransactionsNumber = failedTransactionsNumber;
    }
}
