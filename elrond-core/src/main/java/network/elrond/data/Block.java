package network.elrond.data;

import net.tomp2p.peers.PeerAddress;
import network.elrond.AsciiTable;
import network.elrond.core.Util;
import network.elrond.sharding.Shard;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The Block abstract class implements a block of data that contains hashes of transaction
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-14
 */
public class Block implements Serializable, AsciiPrintable {
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

    @Override
    public AsciiTable print() {

        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(200);

        table.getColumns().add(new AsciiTable.Column("Block "));
        table.getColumns().add(new AsciiTable.Column(nonce + ""));

        AsciiTable.Row rowS = new AsciiTable.Row();
        rowS.getValues().add("Shard");
        rowS.getValues().add(shard.getIndex() + "");
        table.getData().add(rowS);


        AsciiTable.Row row0 = new AsciiTable.Row();
        row0.getValues().add("Nonce");
        row0.getValues().add(nonce.toString());
        table.getData().add(row0);

        AsciiTable.Row row1 = new AsciiTable.Row();
        row1.getValues().add("State Hash");
        row1.getValues().add(Util.getDataEncoded64(appStateHash));
        table.getData().add(row1);

        AsciiTable.Row row2 = new AsciiTable.Row();
        row2.getValues().add("Signature");
        row2.getValues().add(Util.getDataEncoded64(signature));
        table.getData().add(row2);

        AsciiTable.Row row3 = new AsciiTable.Row();
        row3.getValues().add("Commitment");
        row3.getValues().add(Util.getDataEncoded64(commitment));
        table.getData().add(row3);

        AsciiTable.Row row4 = new AsciiTable.Row();
        row4.getValues().add("Prev block");
        row4.getValues().add(Util.getDataEncoded64(prevBlockHash));
        table.getData().add(row4);

        AsciiTable.Row row5 = new AsciiTable.Row();
        row5.getValues().add("Transactions in block");
        row5.getValues().add(listTXHashes.size() + "");
        table.getData().add(row5);

        AsciiTable.Row row6 = new AsciiTable.Row();
        row6.getValues().add("----------------------");
        row6.getValues().add("----------------------------------------------------------------");
        table.getData().add(row6);


        for (int index = 0; index < listTXHashes.size(); index++) {
            byte[] tx = listTXHashes.get(index);
            AsciiTable.Row row7 = new AsciiTable.Row();
            row7.getValues().add("#" + index);
            row7.getValues().add(Util.getDataEncoded64(tx));
            table.getData().add(row7);
        }

        AsciiTable.Row row8 = new AsciiTable.Row();
        row8.getValues().add("----------------------");
        row8.getValues().add("----------------------------------------------------------------");
        table.getData().add(row8);

        AsciiTable.Row row9 = new AsciiTable.Row();
        row9.getValues().add("Peers in block");
        row9.getValues().add(peers.size() + "");
        table.getData().add(row9);

        AsciiTable.Row row10 = new AsciiTable.Row();
        row10.getValues().add("----------------------");
        row10.getValues().add("----------------------------------------------------------------");
        table.getData().add(row10);

        int index = 0;

        for (String node : peers) {
            index++;
            AsciiTable.Row row11 = new AsciiTable.Row();
            row11.getValues().add("#" + index);
            row11.getValues().add(node);
            table.getData().add(row11);
        }

        table.calculateColumnWidth();
        return table;
    }
}
