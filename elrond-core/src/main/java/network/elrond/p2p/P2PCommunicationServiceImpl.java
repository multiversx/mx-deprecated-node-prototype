package network.elrond.p2p;


import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.net.*;
import java.util.Date;

public class P2PCommunicationServiceImpl implements P2PCommunicationService {
    private static final Logger logger = LogManager.getLogger(P2PCommunicationServiceImpl.class);

    private final int pingConnectionTimeOut = 2000;
    private final int portConnectionTimeOut = 2000;

    public PingResponse getPingResponse(String address, int port) throws Exception {
        long timeStampStart = System.currentTimeMillis();
        long timeStampEnd = 0;
        logger.traceEntry("params: {} {}", address, port);

        PingResponse pingResponse = new PingResponse();

        if (address == null) {
            pingResponse.setErrorMessage("address is null");
            logger.trace("address is null");
            timeStampEnd = System.currentTimeMillis();
            logger.trace("took {} ms", timeStampEnd - timeStampStart);
            return logger.traceExit(pingResponse);
        }

        String[] addr = address.split("\\.");
        if (addr.length != 4) {
            pingResponse.setErrorMessage("address is not valid");
            logger.trace("address is not valid");
            timeStampEnd = System.currentTimeMillis();
            logger.trace("took {} ms", timeStampEnd - timeStampStart);
            return logger.traceExit(pingResponse);
        }

        for (int i = 0; i < 4; i++) {
            try {
                int val = Integer.decode(addr[i]);

                if ((val < 0) || (val > 254)) {
                    pingResponse.setErrorMessage("address is not valid");
                    logger.trace("address is not valid");
                    timeStampEnd = System.currentTimeMillis();
                    logger.trace("took {} ms", timeStampEnd - timeStampStart);
                    return logger.traceExit(pingResponse);
                }
            } catch (Exception ex) {
                pingResponse.setErrorMessage(ex.getLocalizedMessage());
                logger.catching(ex);
                timeStampEnd = System.currentTimeMillis();
                logger.trace("took {} ms", timeStampEnd - timeStampStart);
                return logger.traceExit(pingResponse);
            }
        }

        //step 1. plain ping
        InetAddress inet = InetAddress.getByName(address);
        Date dStart = new Date();

        if (!inet.isReachable(pingConnectionTimeOut)) {
            pingResponse.setErrorMessage("timeout");
            logger.trace("timeout");
            timeStampEnd = System.currentTimeMillis();
            logger.trace("took {} ms", timeStampEnd - timeStampStart);
            return logger.traceExit(pingResponse);
        }

        Date dEnd = new Date();
        pingResponse.setResponseTimeMs(dEnd.getTime() - dStart.getTime());
        pingResponse.setReachablePing(true);

        if (!((port > 1) && (port < 65535))) {
            pingResponse.setErrorMessage("port not valid");
            logger.trace("port is not valid");
            timeStampEnd = System.currentTimeMillis();
            logger.trace("took {} ms", timeStampEnd - timeStampStart);
            return logger.traceExit(pingResponse);
        }

        //set 2. try to open socket on port
        try{
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), portConnectionTimeOut);
            OutputStream out = socket.getOutputStream();

            out.close();
            socket.close();
            pingResponse.setReachablePort(true);
        } catch (Exception ex){
            pingResponse.setErrorMessage(ex.getLocalizedMessage());
            logger.catching(ex);
        }

        timeStampEnd = System.currentTimeMillis();
        logger.trace("took {} ms", timeStampEnd - timeStampStart);
        return logger.traceExit(pingResponse);
    }


}
