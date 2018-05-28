package network.elrond.application;

import network.elrond.data.BootstrapType;

import java.io.Serializable;

public class AppContext implements Serializable {

    private Integer peerId;
    private Integer port;
    private String masterPeerIpAddress;
    private Integer masterPeerPort;

    private String storageBasePath = "main";

    private BootstrapType bootstrapType = BootstrapType.START_FROM_SCRATCH;


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

    public String getStorageBasePath(){return(storageBasePath);}

    public void setStorageBasePath(String storageBasePath) {this.storageBasePath = storageBasePath;}

    public BootstrapType getBootstrapType() { return this.bootstrapType;}

    public void setBootstrapType(BootstrapType bootstrapType) { this.bootstrapType = bootstrapType;}

}