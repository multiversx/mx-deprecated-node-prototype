package network.elrond.data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import network.elrond.crypto.PublicKey;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import network.elrond.core.Util;

/**
 * The Block abstract class implements a block of data that contains hashes of transaction
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public abstract class Block {
    //block counter
    protected BigInteger nonce;
    //plain message hash
    protected byte[] hashNoSig;
    //complete tx hash
    protected byte[] hash;
    //blob of data containing first part of sig
    private byte[] sig1;
    //blob of data containing second part of sig
    private byte[] sig2;
    //list of public keys used in signing. First is the leader that proposed the block
    protected List<String> listPubKeys;
    //previus block hash
    protected byte[] prevBlockHash;
    //list of transaction hashes included in block
    protected List<byte[]> listTXHashes;
    //list of transactions included in block
    protected List<Transaction> listTransactions;
    //int shard ID
    protected int shard;
    //app state hash
    protected byte[] appStateHash;
    //true if the block is correctly signed
    boolean isChecked;

    public Block() {
        nonce = BigInteger.ZERO;
        hashNoSig = new byte[0];
        hash = new byte[0];
        listPubKeys = new ArrayList<String>();
        prevBlockHash = new byte[0];
        listTXHashes = new ArrayList<byte[]>();
        listTransactions = new ArrayList<>();
        shard = 0;
        appStateHash = new byte[0];
        sig1 = new byte[0];
        sig2 = new byte[0];
        isChecked = false;
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
     * @return list of tx hashes
     */
    public List<byte[]> getListTXHashes(){
        return(listTXHashes);
    }

    public void addTXHash(byte[] hash){
        listTXHashes.add(hash);
        listTransactions.add(null);
    }

    /**
     * Gets the list of transactions
     * @return list of txs
     */
    public List<Transaction> getListTransactions() {return listTransactions;}

    /**
     * Set transaction at index
     * @param tx to be written
     * @param idx, position
     */
    public void setTransaction(Transaction tx, int idx){
        listTransactions.set(idx, tx);
    }

    /**
     * Gets te public keys used in signing process of the block
     * @return the list of public keys
     */
    public List<String> getListPublicKeys() { return(listPubKeys);}

    /**
     * Gets the first part of signature of the tx
     * @return sig as byte array
     */
    public byte[] getSig1(){return(sig1);}

    /**
     * Sets the first part of signature of the tx
     * @param sig1 as byte array
     */
    public void setSig1(byte[] sig1){this.sig1 = sig1;}

    /**
     * Gets the second part of signature of the tx
     * @return sig as byte array
     */
    public byte[] getSig2(){return(sig2);}

    /**
     * Sets the second part of signature of the tx
     * @param sig2 as byte array
     */
    public void setSig2(byte[] sig2){this.sig2 = sig2;}

    /**
     * Gets the shard's number
     * @return shard as int
     */
    public int getShard(){return (shard);}

    /**
     * Sets the shard's number
     * @param shard to be set
     */
    public void setShard(int shard) { this.shard = shard;}

    /**
     * Gets the previous block hash
     * @return the previous block hash as byte array
     */
    public byte[] getPrevBlockHash() { return prevBlockHash;}

    /**
     * Sets the previous block hash
     * @param prevBlockHash to be set
     */
    public void setPrevBlockHash(byte[] prevBlockHash) {this.prevBlockHash = prevBlockHash;}

    /**
     * Gets the app state hash
     * @return the app state hash as byte array
     */
    public byte[] getAppStateHash() { return appStateHash;}

    /**
     * Sets the app state hash
     * @param appStateHash to be set
     */
    public void setAppStateHash(byte[] appStateHash) {this.appStateHash = appStateHash;}

    /**
     * Gets the flag if all transactions have been fetched
     * @return true, if all tx hashes have been resolved in transaction list
     */
    public boolean getIsSolved() {

        return ((listTransactions.size() == getListTXHashes().size()) &&
                (listTransactions.size() > 0) &&
                (!listTransactions.contains(null)));

    }

//    public static Block createInstance(String strDataJSON)
//    {
//        Block b = new DataBlock();
//        b.decodeJSON(strDataJSON);
//        if (Arrays.equals(b.prevBlockHash,("GENESIS").getBytes())){
//            return (new GenesisBlock());
//        }
//
//        return (b);
//    }


}
