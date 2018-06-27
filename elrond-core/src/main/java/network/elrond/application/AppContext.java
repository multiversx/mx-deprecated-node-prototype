package network.elrond.application;

import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.BootstrapType;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class AppContext implements Serializable {

    private String nodeName;
    private Integer port;
    private String masterPeerIpAddress;
    private Integer masterPeerPort;
    private String storageBasePath = "main";
    private PrivateKey privateKey;

    private String strAddressMint = "000000000000000000000000000000000000000000000000000000000000000000";
    private BootstrapType bootstrapType = BootstrapType.REBUILD_FROM_DISK;//BootstrapType.START_FROM_SCRATCH;
    

    private List<String> listNTPServers = Arrays.asList("time.google.com", "pool.ntp.org", "time.windows.com");

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        Util.check(!(nodeName==null || nodeName.isEmpty()), "nodeName!=null");
        this.nodeName = nodeName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        Util.check(port>0, "port is negative");
        this.port = port;
    }

    public String getMasterPeerIpAddress() {
        return masterPeerIpAddress;
    }

    public void setMasterPeerIpAddress(String masterPeerIpAddress) {
        Util.check(!(masterPeerIpAddress==null || masterPeerIpAddress.isEmpty()), "masterPeerIpAddress!=null");
        this.masterPeerIpAddress = masterPeerIpAddress;
    }

    public Integer getMasterPeerPort() {
        return masterPeerPort;
    }

    public void setMasterPeerPort(Integer masterPeerPort) {
        Util.check(masterPeerPort>0, "masterPeerPort negative");
        this.masterPeerPort = masterPeerPort;
    }

    public String getStorageBasePath() {
        return (storageBasePath);
    }

    public void setStorageBasePath(String storageBasePath) {
        Util.check(!(storageBasePath==null || storageBasePath.isEmpty()), "storageBasePath!=null");
        this.storageBasePath = storageBasePath;
    }

    public BootstrapType getBootstrapType() {
        return this.bootstrapType;
    }

    public void setBootstrapType(BootstrapType bootstrapType) {
        this.bootstrapType = bootstrapType;
    }


    public String getStrAddressMint() {
        return strAddressMint;
    }

    public void setStrAddressMint(String strAddressMint) {
        Util.check(!(strAddressMint==null || strAddressMint.isEmpty()), "strAddressMint!=null");
        this.strAddressMint = strAddressMint;
    }

    public BigInteger getValueMint() {
        return Util.VALUE_MINTING;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        Util.check(privateKey!=null, "privateKey!=null");
        this.privateKey = privateKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public List<String> getListNTPServers(){
        return (listNTPServers);
    }

}