package network.elrond.application;

import network.elrond.data.BootstrapType;

import java.io.Serializable;
import java.math.BigInteger;

public class AppContext implements Serializable {

    private String nodeName;
    private Integer port;
    private String masterPeerIpAddress;
    private Integer masterPeerPort;

    private String storageBasePath = "main";

    private BootstrapType bootstrapType = BootstrapType.START_FROM_SCRATCH;
    private String strAddressMint = "000000000000000000000000000000000000000000000000000000000000000000";
    private BigInteger valueMint = BigInteger.ZERO;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
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

    public String getStorageBasePath() {
        return (storageBasePath);
    }

    public void setStorageBasePath(String storageBasePath) {
        this.storageBasePath = storageBasePath;
    }

    public BootstrapType getBootstrapType() {
        return this.bootstrapType;
    }

    public void setBootstrapType(BootstrapType bootstrapType) {
        this.bootstrapType = bootstrapType;
    }

    public String getStrAddressMint(){return strAddressMint;}

    public void setStrAddressMint(String strAddressMint) {this.strAddressMint = strAddressMint;}

    public BigInteger getValueMint(){return valueMint;}

    public void setValueMint(BigInteger valueMint){this.valueMint = valueMint;}

}