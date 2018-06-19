package network.elrond.api;

import network.elrond.Application;
import network.elrond.ContextCreator;
import network.elrond.account.AccountAddress;
import network.elrond.api.manager.ElrondWebSocketManager;
import network.elrond.application.AppContext;
import network.elrond.crypto.PKSKPair;
import network.elrond.data.BootstrapType;
import network.elrond.data.Transaction;
import network.elrond.p2p.PingResponse;
import network.elrond.service.AppServiceProvider;
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

    @Autowired
    ElrondWebSocketManager elrondWebSocketManager;


    @RequestMapping(path = "/node/stop", method = RequestMethod.GET)
    public @ResponseBody
    boolean stopNode(
            HttpServletResponse response) {
        Application application = elrondApiNode.getApplication();
        if (application != null) {
            application.stop();
        }

        return true;
    }


    @RequestMapping(path = "/node/start", method = RequestMethod.GET)
    public @ResponseBody
    boolean stopNode(
            HttpServletResponse response,
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

        AppContext context = ContextCreator.createAppContext(nodeName, privateKey, masterPeerIpAddress,
                masterPeerPort, port, bootstrapType, blockchainPath, new BigInteger(mintValue));

        return elrondApiNode.start(context, blockchainPath, blockchainRestorePath);
    }


    @RequestMapping(path = "/node/send", method = RequestMethod.GET)
    public @ResponseBody
    Object send(
            HttpServletResponse response,
            @RequestParam(defaultValue = "0326e7875aadaba270ae93ec40ef4706934d070eb21c9acad4743e31289fa4ebc7")
                    String address,
            @RequestParam(defaultValue = "1") BigInteger value) {

        AccountAddress _add = AccountAddress.fromHexString(address);
        Transaction transaction = elrondApiNode.send(_add, value);
        return (transaction != null) ? AppServiceProvider.getSerializationService().getHashString(transaction) : null;


    }


    @RequestMapping(path = "/node/receipt", method = RequestMethod.GET)
    public @ResponseBody
    Object getReceipt(
            HttpServletResponse response,
            @RequestParam() String transactionHash) {
        return elrondApiNode.getReceipt(transactionHash);

    }


    @RequestMapping(path = "/node/balance", method = RequestMethod.GET)
    public @ResponseBody
    Object getBalance(
            HttpServletResponse response,
            @RequestParam() String address) {

        AccountAddress _add = AccountAddress.fromHexString(address);
        return elrondApiNode.getBalance(_add);

    }

    @RequestMapping(path = "/node/ping", method = RequestMethod.GET)
    public @ResponseBody
    PingResponse ping(
            HttpServletResponse response,
            @RequestParam() String ipAddress,
            @RequestParam() int port
    ) {
        return (elrondApiNode.ping(ipAddress, port));
    }

    @RequestMapping(path = "/node/publickeyandprivatekey", method = RequestMethod.GET)
    public @ResponseBody
    PKSKPair generatePublicAndPrivateKey(
            HttpServletResponse response) {
        return elrondApiNode.generatePublicKeyAndPrivateKey();
    }

    @RequestMapping(path = "/node/publickeyfromprivatekey", method = RequestMethod.GET)
    public @ResponseBody
    PKSKPair generatePublicKeyFromPrivateKey(
            HttpServletResponse response,
            @RequestParam() String privateKey) {
        return elrondApiNode.generatePublicKeyFromPrivateKey(privateKey);
    }


}
