package network.elrond.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class ExecutionReport {

    private boolean valid = true;
    private final List<String> entries = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(ExecutionReport.class);

    public ExecutionReport() {
    }

    public static ExecutionReport create() {
        return new ExecutionReport();
    }

    public ExecutionReport ko(String message) {
        entries.add(message);
        logger.error(message);
        valid = false;
        return this;
    }

    public ExecutionReport combine(ExecutionReport report) {
        entries.addAll(report.entries);
        valid &= report.valid;
        return this;
    }

    public ExecutionReport ko(Exception ex) {
        logger.throwing(ex);
        ko(ex.getMessage());
        return this;
    }

    public ExecutionReport ok() {
        valid = true;
        return this;
    }

    public ExecutionReport ok(String message) {
        entries.add(message);
        logger.info(message);
        valid = true;
        return this;
    }

    public boolean isOk() {
        return valid;
    }

    @Override
    public String toString(){
        String status = "NOT OK";
        if (isOk()){
            status = "OK";
        }

        return(String.format("Execution report: %s", status));
    }
}
