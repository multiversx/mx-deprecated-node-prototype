package network.elrond.api;

import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

@Controller
public class ElrondNodeController {


    @Autowired
    ElrondApiNode elrondApiNode;


    @RequestMapping(path = "/node/start", method = RequestMethod.GET)
    public @ResponseBody
    void start(HttpServletResponse response,
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

        elrondApiNode.start(context);

    }


    @RequestMapping(path = "/node/send", method = RequestMethod.GET)
    public @ResponseBody
    Object send(HttpServletResponse response,
                @RequestParam(defaultValue = "0326e7875aadaba270ae93ec40ef4706934d070eb21c9acad4743e31289fa4ebc7")
                        String address,
                @RequestParam(defaultValue = "1") BigInteger value) {

        AccountAddress _add = AccountAddress.fromHexaString(address);
        return elrondApiNode.send(_add, value);

    }

    @RequestMapping(path = "/node/balance", method = RequestMethod.GET)
    public @ResponseBody
    Object send(HttpServletResponse response,
                @RequestParam() String address) {

        AccountAddress _add = AccountAddress.fromHexaString(address);
        return elrondApiNode.getBalance(_add);

    }

}
