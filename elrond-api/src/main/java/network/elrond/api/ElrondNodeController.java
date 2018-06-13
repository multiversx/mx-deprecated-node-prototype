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
import network.elrond.api.manager.ElrondWebSocketManager;
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

    @Autowired
    ElrondWebSocketManager elrondWebSocketManager;


    @RequestMapping(path = "/node/stop", method = RequestMethod.GET)
    public @ResponseBody
    boolean startNode(HttpServletResponse response) {
        Application application = elrondApiNode.getApplication();
        if (application != null) {
            application.stop();
        }

        return true;
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


        PrivateKey privateKey1 = new PrivateKey(Util.hexStringToByteArray(privateKey));
        PublicKey publicKey = new PublicKey(privateKey1);
        context.setPrivateKey(privateKey1);
        String mintAddress = Util.getAddressFromPublicKey(publicKey.getValue());
        context.setStrAddressMint(mintAddress);



        //log appender
        String filterDataAccept = "elrond|tom";
        String filterDataDeny = "tom";

        LoggerUtils loggerUtils = new LoggerUtils();
        loggerUtils.AddLogAppenderWebSockets(this.elrondWebSocketManager, Level.DEBUG);
        loggerUtils.AddLogAppenderRollingFile(Level.DEBUG);

        loggerUtils.AddLogFilterAccept(filterDataAccept);
        loggerUtils.AddLogFilterDeny(filterDataDeny);

        return elrondApiNode.start(context);
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


//    @MessageMapping("/chat.sendMessage")
//    @SendTo("/topic/public")
//    public String sendMessage(@Payload String message) {
//
//        elrondWebSocketManager.announce("/topic/public", message);
//        return "asdas";
//    }
}
