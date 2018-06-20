package network.elrond.p2p;

public class PingResponse {
    private long responseTimeMs;
    private boolean reachablePing;
    private boolean reachablePort;
    private String errorMessage;

    public PingResponse(){
        responseTimeMs = 0;
        reachablePing = false;
        reachablePort = false;
        errorMessage = "";
    }

    public long getReponseTimeMs(){
        return (responseTimeMs);
    }

    public void setResponseTimeMs(long responseTimeMs){
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isReachablePing(){
        return reachablePing;
    }

    public void setReachablePing(boolean reachablePing){
        this.reachablePing = reachablePing;
    }

    public boolean isReachablePort(){
        return(reachablePort);
    }

    public void setReachablePort(boolean reachablePort){
        this.reachablePort = reachablePort;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString(){
        return String.format("PingResponse{ping=%b, response time=%d, reachable port=%b, error=%s}",
                isReachablePing(), responseTimeMs, isReachablePort(), errorMessage);
    }


}
