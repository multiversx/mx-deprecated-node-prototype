package network.elrond.core;

import ch.qos.logback.core.Appender;

import java.io.OutputStream;
import java.util.List;

public interface LoggerService {
    OutputStream getLoggerStream(String identifier) throws Exception;

    Appender getLoggerAppender(String identifier) throws Exception;

    List<Appender> getLoggerAppenderList();
}
