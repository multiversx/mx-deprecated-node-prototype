package network.elrond.core;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;


import java.io.OutputStream;
import java.util.List;

public class LoggerServiceImpl implements LoggerService{
    public OutputStream getLoggerStream(String identifier) throws Exception{

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender appender = logger.getAppender(identifier);

        if (appender.getClass().getName().compareToIgnoreCase(ByteArrayOutputStreamAppender.class.getName()) == 0){
            return ((ByteArrayOutputStreamAppender)appender).getMainOutputStream();
        }

        throw new IllegalArgumentException("Appender " + identifier + " not found (see logback.xml) or is not an ByteArrayOutputStreamAppender appender!");
    }

    public Appender getLoggerAppender(String identifier) throws Exception{

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender appender = logger.getAppender(identifier);

        if (appender.getClass().getName().compareToIgnoreCase(ByteArrayOutputStreamAppender.class.getName()) == 0){
            return (appender);
        }

        throw new IllegalArgumentException("Appender " + identifier + " not found (see logback.xml) or is not an ByteArrayOutputStreamAppender appender!");
    }

}
