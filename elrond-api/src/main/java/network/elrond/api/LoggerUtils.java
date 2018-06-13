package network.elrond.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.*;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.FileSize;
import network.elrond.api.manager.ElrondWebSocketManager;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class LoggerUtils {
    private String logPattern;
    private FileSize maxFileSize;

    public LoggerUtils(){
        logPattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n";
        maxFileSize = new FileSize(4*1024*1024); //4 MB
    }

    public void setLogPattern(String logPattern){
        this.logPattern = logPattern;
    }

    public String getLogPattern(){
        return(this.logPattern);
    }

    public FileSize getMaxFileSize(){
        return(maxFileSize);
    }

    public void setMaxFileSize(FileSize maxFileSize){
        this.maxFileSize = maxFileSize;
    }

    public void AddLogAppenderWebSockets(ElrondWebSocketManager elrondWebSocketManager, Level levelToBeAdded){
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        //ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setPattern(logPattern);
        ple.setContext(lc);
        ple.start();

        WebSocketAppender webSocketAppender = new WebSocketAppender();
        webSocketAppender.setEncoder(ple);
        webSocketAppender.setContext(lc);
        webSocketAppender.setElrondWebSocketManager(elrondWebSocketManager);

        webSocketAppender.setName("wslogger");
        webSocketAppender.start();

        Logger logbackLogger =
                (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(webSocketAppender);
        logbackLogger.setLevel(levelToBeAdded);
        logbackLogger.setAdditive(false);
    }

    public void AddLogAppenderRollingFile(Level levelToBeAdded){
        String logPath = "";

        try {
            logPath = createLogFolderAndReturnPath();
        } catch(Exception ex){
            System.out.println("Failed to create rolling file appender (IO error)");
            ex.printStackTrace();
        }

        AddLogAppenderRollingFile(logPath, levelToBeAdded);
    }

    public void AddLogAppenderRollingFile(String logPath, Level levelToBeAdded){

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        //ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setPattern(logPattern);
        ple.setContext(lc);
        ple.start();

        File filePattern = new File(logPath, "app.%i.log");
        File fileMain = new File(logPath, "app.log");

        RollingFileAppender rollingFileAppender = new RollingFileAppender();
        rollingFileAppender.setName("rflogger");
        rollingFileAppender.setFile(fileMain.toString());


        RollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        ((FixedWindowRollingPolicy) rollingPolicy).setFileNamePattern(filePattern.toString());
        rollingPolicy.setParent(rollingFileAppender);
        ((FixedWindowRollingPolicy) rollingPolicy).setContext(lc);

        TriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy();
        ((SizeBasedTriggeringPolicy) triggeringPolicy).setMaxFileSize(maxFileSize);
        ((SizeBasedTriggeringPolicy) triggeringPolicy).setContext(lc);

        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(ple);
        rollingFileAppender.setContext(lc);
        triggeringPolicy.start();
        rollingPolicy.start();
        rollingFileAppender.start();

        Logger logbackLogger =
                (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(rollingFileAppender);
        logbackLogger.setLevel(levelToBeAdded);
        logbackLogger.setAdditive(false);
    }

    public void AddLogFilterAccept(String filterDataAccept){
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        Filter filterAccept = getFilterAccept(filterDataAccept);
        //Filter filterDeny = getFilterDeny(filterDataDeny);

        for (Logger logger : lc.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                Appender<ILoggingEvent> appender = index.next();

                appender.addFilter(filterAccept);
                //appender.addFilter(filterDeny);
            }
        }
    }

    public void AddLogFilterDeny(String filterDataDeny){
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        Filter filterDeny = getFilterDeny(filterDataDeny);

        for (Logger logger : lc.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                Appender<ILoggingEvent> appender = index.next();

                appender.addFilter(filterDeny);
            }
        }
    }

    private Filter getFilterAccept(String filterAccept) {
        String[] dataToAccept = filterAccept.split("\\|");
        Filter filter = new Filter() {

            @Override
            public FilterReply decide(Object event) {
                if (filterAccept.equals("*")) {
                    return (FilterReply.NEUTRAL);
                }

                if (event.getClass().getName().equals(LoggingEvent.class.getName())) {
                    LoggingEvent loggingEvent = (LoggingEvent) event;

                    for (int i = 0; i < dataToAccept.length; i++) {
                        if (loggingEvent.getLoggerName().contains(dataToAccept[i])) {
                            return (FilterReply.NEUTRAL);
                        }
                    }
                }

                for (int i = 0; i < dataToAccept.length; i++) {
                    if (event.toString().contains(dataToAccept[i])) {
                        return (FilterReply.NEUTRAL);
                    }
                }

                return (FilterReply.DENY);
            }
        };
        filter.start();

        return (filter);
    }

    private Filter getFilterDeny(String filterDeny) {
        String[] dataToAccept = filterDeny.split("\\|");
        Filter filter = new Filter() {

            @Override
            public FilterReply decide(Object event) {
                if (filterDeny.equals("")) {
                    return (FilterReply.NEUTRAL);
                }

                if (event.getClass().getName().equals(LoggingEvent.class.getName())) {
                    LoggingEvent loggingEvent = (LoggingEvent) event;

                    for (int i = 0; i < dataToAccept.length; i++) {
                        if (loggingEvent.getLoggerName().contains(dataToAccept[i])) {
                            return (FilterReply.DENY);
                        }
                    }
                }

                for (int i = 0; i < dataToAccept.length; i++) {
                    if (event.toString().contains(dataToAccept[i])) {
                        return (FilterReply.DENY);
                    }
                }

                return (FilterReply.NEUTRAL);
            }
        };
        filter.start();

        return (filter);
    }

    public String createLogFolderAndReturnPath() throws IOException {
        File filePathLogs = new File(new File(".").getCanonicalPath(), "logs");

        if (!filePathLogs.isDirectory()){
            filePathLogs.mkdir();
        }

        return(filePathLogs.getAbsolutePath());
    }
}
