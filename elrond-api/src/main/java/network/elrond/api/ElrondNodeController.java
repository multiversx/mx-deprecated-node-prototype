package network.elrond.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.Util;
import network.elrond.crypto.PKSKPair;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.p2p.PingResponse;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.mapdb.Fun;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

@Controller
public class ElrondNodeController {

    @Autowired
    ElrondApiNode elrondApiNode;

    @RequestMapping(path = "/node/start", method = RequestMethod.GET)
    public @ResponseBody
    void startNode(HttpServletResponse response,
                   @RequestParam(defaultValue = "elrond-node-1") String nodeName,
                   @RequestParam(defaultValue = "4001") Integer port,
                   @RequestParam(defaultValue = "4000", required = false) Integer masterPeerPort,
                   @RequestParam(defaultValue = "127.0.0.1", required = false) String masterPeerIpAddress,
                   @RequestParam(defaultValue = "026c00d83e0dc47e6b626ed6c42f636b", required = true) String privateKey

    ) {

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress(masterPeerIpAddress);
        context.setMasterPeerPort(masterPeerPort);
        context.setPort(port);
        context.setNodeName(nodeName);

        PrivateKey privateKey1 = new PrivateKey(privateKey);
        PublicKey publicKey = new PublicKey(privateKey1);

        context.setPrivateKey(privateKey1);
        String mintAddress = Util.getAddressFromPublicKey(publicKey.getValue());
        context.setStrAddressMint(mintAddress);

        context.setValueMint(Util.VALUE_MINTING);

        //log appender


        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();

        WebSocketAppender webSocketAppender = new WebSocketAppender();
        webSocketAppender.setEncoder(ple);
        webSocketAppender.setContext(lc);
        webSocketAppender.setElrondWebsocketManager(elrondApiNode.getElrondWebsocketManager());
        webSocketAppender.setName("logger");
        webSocketAppender.start();

        ch.qos.logback.classic.Logger logbackLogger =
                (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(webSocketAppender);
        logbackLogger.setLevel(Level.DEBUG);
        logbackLogger.setAdditive(false);

        elrondApiNode.start(context);






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
