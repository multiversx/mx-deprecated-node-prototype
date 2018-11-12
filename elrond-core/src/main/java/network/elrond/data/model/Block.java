package network.elrond.data.model;

import network.elrond.core.Util;
import network.elrond.sharding.Shard;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
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
    //listToTable of public keys used in signing. First is the leader that proposed the block
    protected List<String> listPubKeys;
    //previus block hash
    protected byte[] prevBlockHash;
    //listToTable of transaction hashes included in block
    protected List<byte[]> listTXHashes;
    //hashset of peers addresses included in block
    protected List<String> peers;

    //int shard ID
    protected Shard shard;
    //app state hash
    protected byte[] appStateHash;

    protected long timestamp = 0;

    protected long roundIndex = 0;

    public Block() {
        nonce = BigInteger.ZERO;
        listPubKeys = new ArrayList<String>();
        prevBlockHash = new byte[0];
        listTXHashes = new ArrayList<byte[]>();
        peers = new ArrayList<String>();
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
     * Gets the listToTable of transaction hashes
     *
     * @return listToTable of tx hashes
     */
    public List<byte[]> getListTXHashes() {
        return (listTXHashes);
    }

    public void setListTXHashes(List<byte[]> listTXHashes) {
        this.listTXHashes = listTXHashes;
    }

    public List<String> getPeers() {
        return peers;
    }

    public void setPeers(List<String> peers) {
        this.peers = peers;
    }

    /**
     * Gets te public keys used in signing process of the block
     *
     * @return the listToTable of public keys
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
     */
    public Shard getShard() {
        return (shard);
    }

    /**
     * Sets the shard's number
     */
    public void setShard(Shard shard) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getRoundIndex() {
        return (roundIndex);
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    @Override
    public String toString() {
        return (String.format("Block{shard=%s, nonce=%d, signature='%s', commitment='%s', appStateHash='%s', listTXHashes.size=%d, roundIndex=%d, timestamp=%d}",
                shard, nonce, Util.byteArrayToHexString(signature), Util.byteArrayToHexString(commitment), Util.byteArrayToHexString(appStateHash), listTXHashes.size(), roundIndex, timestamp));
    }
}
