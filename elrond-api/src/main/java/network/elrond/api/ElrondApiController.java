package network.elrond.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
public class ElrondApiController {

    @RequestMapping(path = "/node", method = RequestMethod.POST)
    public @ResponseBody
    Object foo(HttpServletResponse response, @RequestParam String arguments) {
        return null;
    }

}
