package network.elrond.p2p;

import net.tomp2p.peers.Number160;

import java.io.Serializable;
import java.net.InetAddress;

public class PeerAddressShard implements Serializable {
    private byte[] address;
    private int port;
    private Number160 id;
    private int shard;

    public PeerAddressShard(){
        address = new byte[]{127, 0, 0, 1};
        port = 0;
        id = Number160.ZERO;
        shard = 0;
    }

    public InetAddress getAddress() throws Exception{
        return InetAddress.getByAddress(address);
    }

    public void setAddress(InetAddress inetAddress){
        address = inetAddress.getAddress();
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }

    public Number160 getId(){
        return id;
    }

    public void setId(Number160 id){
        this.id = id;
    }

    public int getShard(){
        return shard;
    }

    public void setShard(int shard){
        this.shard = shard;
    }
}
