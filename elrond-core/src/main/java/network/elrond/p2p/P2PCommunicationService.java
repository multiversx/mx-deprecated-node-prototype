package network.elrond.p2p;

public interface P2PCommunicationService {
    PingResponse getPingResponse(String address, int port) throws Exception;

    boolean isPortReachable(String address, int port, int timeoutPeriod);
}
