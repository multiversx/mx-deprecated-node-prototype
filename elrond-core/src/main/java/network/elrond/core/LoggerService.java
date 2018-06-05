package network.elrond.core;

import ch.qos.logback.core.Appender;

import java.io.OutputStream;

public interface LoggerService {
    OutputStream getLoggerStream(String identifier) throws Exception;

    Appender getLoggerAppender(String identifier) throws Exception;
}
