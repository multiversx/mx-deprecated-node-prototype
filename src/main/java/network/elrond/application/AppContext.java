package network.elrond.application;

import java.io.Serializable;

public class AppContext implements Serializable {

    private Integer peerId;
    private Integer port;
    private String masterPeerIpAddress;
    private Integer masterPeerPort;




    public Integer getPeerId() {
        return peerId;
    }

    public void setPeerId(Integer peerId) {
        this.peerId = peerId;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getMasterPeerIpAddress() {
        return masterPeerIpAddress;
    }

    public void setMasterPeerIpAddress(String masterPeerIpAddress) {
        this.masterPeerIpAddress = masterPeerIpAddress;
    }

    public Integer getMasterPeerPort() {
        return masterPeerPort;
    }

    public void setMasterPeerPort(Integer masterPeerPort) {
        this.masterPeerPort = masterPeerPort;
    }


}