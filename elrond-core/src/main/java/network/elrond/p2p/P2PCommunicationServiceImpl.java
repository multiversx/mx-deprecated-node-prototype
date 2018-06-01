package network.elrond.p2p;


import network.elrond.core.Util;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

public class P2PCommunicationServiceImpl implements P2PCommunicationService {
    public PingResponse getPingResponse(String address, int port) throws Exception{
        Util.check(address != null, "address is null");
        Util.check((port > 1) && (port < 65535), "port not valid");

        String[] addr = address.split("\\.");
        Util.check(addr.length == 4, "addess is not valid");

        for (int i = 0; i < 4; i++){
            try {
                int val = Integer.decode(addr[i]);

                if ((val < 0) || (val > 254)){
                    Util.check(false, "addess is not valid");
                }
            } catch (Exception ex){
                Util.check(false, "addess is not valid");
            }
        }

        //step 1. plain ping
        InetAddress inet = InetAddress.getByName(address);
        Date dStart = new Date();

        PingResponse pingResponse = new PingResponse();

        if (!inet.isReachable(5000)){
            pingResponse.setReachablePing(false);
            pingResponse.setReachablePort(false);
            pingResponse.setResponseTimeMs(0);

            return (pingResponse);
        }

        Date dEnd = new Date();
        pingResponse.setResponseTimeMs(dEnd.getTime() - dStart.getTime());
        pingResponse.setReachablePing(true);

        //set 2. try to open socket on port
        try{
            Socket socket = new Socket(address, port);
            OutputStream out = socket.getOutputStream();

            out.close();
            socket.close();
            pingResponse.setReachablePort(true);
        } catch (Exception ex){
            pingResponse.setReachablePort(false);
        }

        return(pingResponse);
    }


}
