package network.elrond.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutionReport {

    private boolean valid = true;
    private final List<String> entries = new ArrayList<>();
    private final Logger logger = Logger.getLogger(ExecutionReport.class.getName());


    public ExecutionReport() {
    }

    public static ExecutionReport create() {
        return new ExecutionReport();
    }

    public ExecutionReport ko(String message) {
        entries.add(message);
        logger.log(Level.SEVERE, message);
        valid = false;
        return this;
    }

    public ExecutionReport combine(ExecutionReport report) {
        entries.addAll(report.entries);
        valid &= report.valid;
        return this;
    }

    public ExecutionReport ko(Exception ex) {
        ex.printStackTrace();
        ko(ex.getMessage());
        return this;
    }

    public ExecutionReport ok() {
        valid = true;
        return this;
    }

    public ExecutionReport ok(String message) {
        entries.add(message);
        logger.log(Level.INFO, message);
        valid = true;
        return this;
    }

    public boolean isOk() {
        return valid;
    }


}
