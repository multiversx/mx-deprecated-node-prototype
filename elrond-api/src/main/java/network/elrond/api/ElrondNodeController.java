package network.elrond.api;

import network.elrond.application.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
public class ElrondNodeController {


    @Autowired
    ElrondApplicationRunner elrondApplicationRunner;


    @RequestMapping(path = "/node/start", method = RequestMethod.GET)
    public @ResponseBody
    Object foo(HttpServletResponse response,
               @RequestParam(defaultValue = "elrond-node-1")
                       String nodeName,
               @RequestParam(defaultValue = "4001")
                       Integer port,
               @RequestParam(defaultValue = "4000")
                       Integer masterPeerPort,
               @RequestParam(defaultValue = "127.0.0.1")
                       String masterPeerIpAddress) {


        AppContext context = new AppContext();
        context.setMasterPeerIpAddress(masterPeerIpAddress);
        context.setMasterPeerPort(masterPeerPort);
        context.setPort(port);
        context.setNodeName(nodeName);

        elrondApplicationRunner.start(context);

        return "";
    }

}
