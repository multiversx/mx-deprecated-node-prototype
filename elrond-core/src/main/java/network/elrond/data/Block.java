package network.elrond.data;

import network.elrond.core.Util;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Block abstract class implements a block of data that contains hashes of transaction
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-14
 */
public class Block implements Serializable {
    //block counter
    protected BigInteger nonce;
    //blob of data containing first part of sig
    private byte[] signature;
    //blob of data containing second part of sig
    private byte[] commitment;
    //list of public keys used in signing. First is the leader that proposed the block
    protected List<String> listPubKeys;
    //previus block hash
    protected byte[] prevBlockHash;
    //list of transaction hashes included in block
    protected List<byte[]> listTXHashes;
    //int shard ID
    protected int shard;
    //app state hash
    protected byte[] appStateHash;

    protected Date timestamp = new Date();

    public Block() {
        nonce = BigInteger.ZERO;
//        hashNoSig = new byte[0];
//        hash = new byte[0];
        listPubKeys = new ArrayList<String>();
        prevBlockHash = new byte[0];
        listTXHashes = new ArrayList<byte[]>();
        shard = 0;
        appStateHash = new byte[0];
        signature = new byte[0];
        commitment = new byte[0];
    }

    /**
     * Gets the nonce
     *
     * @return nonce as BigInteger
     */
    public BigInteger getNonce() {
        return (nonce);
    }

    /**
     * Sets the nonce
     *
     * @param nonce to be set
     */
    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    /**
     * Gets the list of transaction hashes
     *
     * @return list of tx hashes
     */
    public List<byte[]> getListTXHashes() {
        return (listTXHashes);
    }

    public void setListTXHashes(List<byte[]> listTXHashes) {
        this.listTXHashes = listTXHashes;
    }

    /**
     * Gets te public keys used in signing process of the block
     *
     * @return the list of public keys
     */
    public List<String> getListPublicKeys() {
        return (listPubKeys);
    }

    public void setListPubKeys(List<String> listPubKeys) {
        this.listPubKeys = listPubKeys;
    }

    /**
     * Gets the first part of signature of the tx
     *
     * @return sig as byte array
     */
    public byte[] getSignature() {
        return (signature);
    }

    /**
     * Sets the first part of signature of the tx
     *
     * @param signature as byte array
     */
    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    /**
     * Gets the second part of signature of the tx
     *
     * @return sig as byte array
     */
    public byte[] getCommitment() {
        return (commitment);
    }

    /**
     * Sets the second part of signature of the tx
     *
     * @param commitment as byte array
     */
    public void setCommitment(byte[] commitment) {
        this.commitment = commitment;
    }

    /**
     * Gets the shard's number
     *
     * @return shard as int
     */
    public int getShard() {
        return (shard);
    }

    /**
     * Sets the shard's number
     *
     * @param shard to be set
     */
    public void setShard(int shard) {
        this.shard = shard;
    }

    /**
     * Gets the previous block hash
     *
     * @return the previous block hash as byte array
     */
    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    /**
     * Sets the previous block hash
     *
     * @param prevBlockHash to be set
     */
    public void setPrevBlockHash(byte[] prevBlockHash) {
        this.prevBlockHash = prevBlockHash;
    }

    /**
     * Gets the app state hash
     *
     * @return the app state hash as byte array
     */
    public byte[] getAppStateHash() {
        return appStateHash;
    }

    /**
     * Sets the app state hash
     *
     * @param appStateHash to be set
     */
    public void setAppStateHash(byte[] appStateHash) {
        this.appStateHash = appStateHash;
    }

    public Date getTimestamp() {
        return timestamp;
    }


    @Override
    public String toString() {
        return "Block{" +
                "nonce=" + nonce +
                ", appStateHash=" + Util.byteArrayToHexString(appStateHash) +
                '}';
    }
}
