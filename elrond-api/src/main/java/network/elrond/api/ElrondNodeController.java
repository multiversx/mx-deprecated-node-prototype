package network.elrond.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.Util;
import network.elrond.crypto.PKSKPair;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.BootstrapType;
import network.elrond.p2p.PingResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;

@Controller
public class ElrondNodeController {

    @Autowired
    ElrondApiNode elrondApiNode;


    @RequestMapping(path = "/node/stop", method = RequestMethod.GET)
    public @ResponseBody
    void startNode(HttpServletResponse response) {
        Application application = elrondApiNode.getApplication();
        if (application != null) {
            application.stop();
        }
    }


    @RequestMapping(path = "/node/start", method = RequestMethod.GET)
    public @ResponseBody
    boolean startNode(HttpServletResponse response,
                      @RequestParam(defaultValue = "elrond-node-1") String nodeName,
                      @RequestParam(defaultValue = "4001") Integer port,
                      @RequestParam(defaultValue = "4000", required = false) Integer masterPeerPort,
                      @RequestParam(defaultValue = "127.0.0.1", required = false) String masterPeerIpAddress,
                      @RequestParam(defaultValue = "026c00d83e0dc47e6b626ed6c42f636b", required = true) String privateKey,
                      @RequestParam(defaultValue = "21000000", required = false) String mintValue,
                      @RequestParam(defaultValue = "START_FROM_SCRATCH", required = true) BootstrapType bootstrapType,
                      @RequestParam(defaultValue = "elrond-node-1", required = false) String blockchainPath,
                      @RequestParam(defaultValue = "elrond-node-1", required = false) String blockchainRestorePath

    ) {

        AppContext context = new AppContext();

        context.setMasterPeerIpAddress(masterPeerIpAddress);
        context.setMasterPeerPort(masterPeerPort);
        context.setPort(port);
        context.setNodeName(nodeName);
        context.setValueMint(BigInteger.valueOf(Long.valueOf(mintValue)));
        context.setStorageBasePath(blockchainPath);

        context.setBootstrapType(bootstrapType);

        if (bootstrapType.equals(BootstrapType.REBUILD_FROM_DISK)) {
            setupRestoreDir(new File(blockchainRestorePath), new File(blockchainPath));
        }


        PrivateKey privateKey1 = new PrivateKey(privateKey);
        PublicKey publicKey = new PublicKey(privateKey1);

        context.setPrivateKey(privateKey1);
        String mintAddress = Util.getAddressFromPublicKey(publicKey.getValue());
        context.setStrAddressMint(mintAddress);

        context.setValueMint(Util.VALUE_MINTING);

        //log appender
        String filterDataAccept = "elrond|tom";
        String filterDataDeny = "tom";


        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        //ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n");
        ple.setContext(lc);
        ple.start();

        WebSocketAppender webSocketAppender = new WebSocketAppender();
        webSocketAppender.setEncoder(ple);
        webSocketAppender.setContext(lc);
        webSocketAppender.setEchoWebSocketServer(elrondApiNode.getEchoWebSocketServer());
        //webSocketAppender.setElrondWebsocketManager(elrondApiNode.getElrondWebsocketManager());
        webSocketAppender.setName("logger");
        webSocketAppender.start();

        Logger logbackLogger =
                (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(webSocketAppender);
        logbackLogger.setLevel(Level.DEBUG);
        logbackLogger.setAdditive(false);

        Filter filterAccept = getFilterAccept(filterDataAccept);
        Filter filterDeny = getFilterDeny(filterDataDeny);

        for (Logger logger : lc.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                Appender<ILoggingEvent> appender = index.next();

                appender.addFilter(filterAccept);
                appender.addFilter(filterDeny);
            }
        }


        return elrondApiNode.start(context);
    }

    private Filter getFilterAccept(String filterAccept) {
        String[] dataToAccept = filterAccept.split("\\|");
        Filter filter = new Filter() {

            @Override
            public FilterReply decide(Object event) {
                if (filterAccept.equals("*")) {
                    return (FilterReply.NEUTRAL);
                }

                if (event.getClass().getName() == LoggingEvent.class.getName()) {
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

                if (event.getClass().getName() == LoggingEvent.class.getName()) {
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

    private void setupRestoreDir(File sourceDir, File destinationDir) {
        if (!sourceDir.getAbsolutePath().equals(destinationDir.getAbsolutePath())) {
            deleteDirectory(destinationDir);
            copyDirectory(sourceDir, destinationDir);
        }
    }

    private void copyDirectory(File src, File dest) {
        try {
            FileUtils.copyDirectory(src, dest);
        } catch (IOException ex) {
            System.out.println("Copy directory exception");
            ex.printStackTrace();
        }
    }

    private void deleteDirectory(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException ex) {
            System.out.println("Delete directory exception");
            ex.printStackTrace();
        }
    }

    @RequestMapping(path = "/node/send", method = RequestMethod.GET)
    public @ResponseBody
    Object send(HttpServletResponse response,
                @RequestParam(defaultValue = "0326e7875aadaba270ae93ec40ef4706934d070eb21c9acad4743e31289fa4ebc7")
                        String address,
                @RequestParam(defaultValue = "1") BigInteger value) {

        AccountAddress _add = AccountAddress.fromHexString(address);
        return elrondApiNode.send(_add, value);

    }

    @RequestMapping(path = "/node/balance", method = RequestMethod.GET)
    public @ResponseBody
    Object getBalance(HttpServletResponse response,
                      @RequestParam() String address) {

        AccountAddress _add = AccountAddress.fromHexString(address);
        return elrondApiNode.getBalance(_add);

    }

    @RequestMapping(path = "/node/ping", method = RequestMethod.GET)
    public @ResponseBody
    PingResponse ping(HttpServletResponse response,
                      @RequestParam() String ipAddress,
                      @RequestParam() int port
    ) {
        return (elrondApiNode.ping(ipAddress, port));
    }

    @RequestMapping(path = "/node/publickeyandprivatekey", method = RequestMethod.GET)
    public @ResponseBody
    PKSKPair generatePublicAndPrivateKey(HttpServletResponse response) {
        return elrondApiNode.generatePublicKeyAndPrivateKey();
    }

    @RequestMapping(path = "/node/publickeyfromprivatekey", method = RequestMethod.GET)
    public @ResponseBody
    PKSKPair generatePublicKeyFromPrivateKey(HttpServletResponse response,
                                             @RequestParam() String privateKey) {
        return elrondApiNode.generatePublicKeyFromPrivateKey(privateKey);
    }
}
