package network.elrond.chronology;

/*
This is a modified version of example by Jason Mathews, MITRE Corp that was
published on https://commons.apache.org/proper/commons-net/index.html
with the Apache Commons Net software.
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import network.elrond.core.Util;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * NTPClient polls an NTP server with UDP  and returns milli seconds with
 * currentTimeMillis() intended as drop in replacement for System.currentTimeMillis()
 *
 * @author Will Shackleford
 */

public class NTPClient implements AutoCloseable{
    private static final Logger logger = LogManager.getLogger(NTPClient.class);

    //final InetAddress hostAddr;
    NTPUDPClient ntpUdpClient;
    Thread pollThread = null;
    long pollMs;
    List<InetAddress> listHostsAddr = new ArrayList<>();
    List<String> listHosts = new ArrayList();
    int currentHost = 0;
    boolean offline = true;

    private void pollNtpServer() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(pollMs);
                    TimeInfo ti = ntpUdpClient.getTime(listHostsAddr.get(currentHost));
                    this.setTimeInfo(ti);
                    offline = false;
                } catch (SocketTimeoutException ste) {
                    logger.trace("Failed to reach NTPServer {}, trying nex server from listToTable!", getCurrentHostName());
                    currentHost++;
                    currentHost = currentHost % listHostsAddr.size();
                    offline = true;
                }
            }
        } catch (InterruptedException interruptedException) {
        } catch (IOException ex) {
            logger.throwing(ex);
        }
    }

    /**
     * Connect to host and poll the host every poll_ms milliseconds.
     * Thread is started in the constructor.
     * @param listHosts
     * @param pollMs
     * @throws UnknownHostException
     * @throws SocketException
     */
    public NTPClient(List<String> listHosts, int pollMs) throws UnknownHostException, SocketException, NullPointerException {
        logger.traceEntry("params: {} {}", listHosts, pollMs);
        this.pollMs = pollMs;

        Util.check(listHosts != null, "listHosts should not be null!");

        StringBuilder stringBuilderHosts = new StringBuilder();

        logger.trace("Building internal lists...");
        for (int i = 0; i < listHosts.size(); i++){
            InetAddress host = InetAddress.getByName(listHosts.get(i));
            listHostsAddr.add(host);
            this.listHosts.add(listHosts.get(i));
            if (i > 0){
                stringBuilderHosts.append(", ");
            }
            stringBuilderHosts.append(host);
        }

        if (listHostsAddr.size() == 0){
            logger.trace("Lists are empty, adding a default, not usable server!");
            listHostsAddr.add(InetAddress.getByName("[N/A]"));
            listHosts.add("[N/A]");
        }

        ntpUdpClient = new NTPUDPClient();
        ntpUdpClient.setDefaultTimeout(10000);
        ntpUdpClient.open();
        ntpUdpClient.setSoTimeout(pollMs * 2 + 20);
        pollThread = new Thread(this::pollNtpServer, "pollNtpServer(" + stringBuilderHosts.toString() + "," + pollMs + ")");
        pollThread.start();
        logger.traceExit();
    }

    private TimeInfo timeInfo;
    private long timeInfoSetLocalTime;
    private Object locker = new Object();

    private void setTimeInfo(TimeInfo timeInfo) {
        synchronized (locker) {
            this.timeInfo = timeInfo;
            timeInfoSetLocalTime = System.currentTimeMillis();
        }
    }

    public long getPollMs(){
        return(pollMs);
    }

    public void setPollMs(int pollMs){
        try {
            ntpUdpClient.setSoTimeout(pollMs * 2 + 20);
            this.pollMs = pollMs;
        } catch (Exception ex){
            System.out.println("Error setting pollMs");
            ex.printStackTrace();
        }
    }

    /**
     * Returns milliseconds just as System.currentTimeMillis() but using the latest
     * estimate from the remote time server.
     * @return the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     */
    public long currentTimeMillis(){
        synchronized (locker) {
            if (timeInfo == null) {
                logger.trace("NTP client is not available, returning system's clock.");
                return (System.currentTimeMillis());
            }

            long diff = System.currentTimeMillis() - timeInfoSetLocalTime;

            return timeInfo.getMessage().getReceiveTimeStamp().getTime() + diff;
        }
    }

    public boolean isOffline(){
        return(offline);
    }

    public String getCurrentHostName(){
        return(listHosts.get(currentHost));
    }

    @Override
    public void close() throws Exception {
        logger.traceEntry();
        if (null != pollThread) {
            pollThread.interrupt();
            pollThread.join(200);
            pollThread = null;
        }
        if (null != ntpUdpClient) {
            ntpUdpClient.close();
            ntpUdpClient = null;
        }
        logger.traceExit();
    }

    protected void finalizer() {
        try {
            this.close();
        } catch (Exception ex) {
            logger.throwing(ex);
        }
    }

}
