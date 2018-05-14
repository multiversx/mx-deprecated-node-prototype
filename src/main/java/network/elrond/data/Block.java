package network.elrond.data;

import java.math.BigInteger;
import java.util.ArrayList;
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
    protected  BigInteger nonce;
    //plain message hash
    protected byte[] hashNoSig;
    //complete tx hash
    protected byte[] hash;
    //blob of data containing sig
    protected byte[] sig;
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

    public Block(){
        nonce = BigInteger.ZERO;
        hashNoSig = new byte[0];
        hash = new byte[0];
        listPubKeys = new ArrayList<String>();
        prevBlockHash = new byte[0];
        listTXHashes = new ArrayList<byte[]>();
        shard = 0;
        appStateHash = new byte[0];
    }

    /**
     * Gets the nonce
     * @return nonce as BigInteger
     */
    public BigInteger getNonce() { return (nonce); }

    /**
     * Sets the nonce
     * @param nonce to be set
     */
    public void setNonce(BigInteger nonce){
        this.nonce = nonce;
    }

    /**
     * Encodes in JSON format the block information using the data from all its fields
     * This is usually used when broadcasting the complete blk to peers
     * @return JSON format of the blk as String
     */
    public String encodeJSON() {
        JSONObject jblk = new JSONObject();

        JSONObject jobj = new JSONObject();
        jobj.put("nonce", nonce.toString(10));

        if (sig == null)
        {
            jobj.put("sig", "");
        } else {
            jobj.put("sig", new String(Base64.encode(sig)));
        }
        //appends the public keys
        for (int i = 0; i < listPubKeys.size(); i++) {
            jobj.append("keys", listPubKeys.get(i));
        }

        //appends the tx hashes
        for (int i = 0; i < listTXHashes.size(); i++) {
            jobj.append("txs", new String(Base64.encode(listTXHashes.get(i))));
        }

        jobj.put("shard", shard);

        //prev block hash
        jobj.put("pbh", new String(Base64.encode(prevBlockHash)));

        //app state hash
        jobj.put("ash", new String(Base64.encode(appStateHash)));

        jblk.put("BLK", jobj);

        return(jblk.toString());
    }

    /**
     *  Encodes in JSON format the block information using sig field as empty string ""
     *  This is usually used to generate the hash without signature for signing/verifying the blk
     * @return JSON format of the blk as String
     */
    public String encodeJSONnoSig() {
        JSONObject jblk = new JSONObject();

        JSONObject jobj = new JSONObject();
        jobj.put("nonce", nonce.toString(10));
        jobj.put("sig", "");

        //appends the public keys
        for (int i = 0; i < listPubKeys.size(); i++) {
            jobj.append("keys", listPubKeys.get(i));
        }

        //appends the tx hashes
        for (int i = 0; i < listTXHashes.size(); i++) {
            jobj.append("txs", new String(Base64.encode(listTXHashes.get(i))));
        }

        jobj.put("shard", shard);

        //prev block hash
        jobj.put("pbh", new String(Base64.encode(prevBlockHash)));

        //app state hash
        jobj.put("ash", new String(Base64.encode(appStateHash)));

        jblk.put("BLK", jobj);

        return(jblk.toString());
    }


    /**
     * Computes the hash of the block with an empty sig field
     * Used in signing/verifying process
     * @return hash as byte array
     */
    public byte[] getHashNoSig() {
        if (hashNoSig.length == 0) {
            //compute hash
            hashNoSig = Util.SHA3.digest(this.encodeJSONnoSig().getBytes());
        }

        return (hashNoSig);
    }

    /**
     * Computes the hash of the complete tx info
     * Used as a mean of blk identification
     * @return hash as byte array
     */
    public byte[] getHash() {
        if (hash.length == 0) {
            //compute hash
            hash = Util.SHA3.digest(this.encodeJSON().getBytes());
        }

        return (hash);
    }

    /**
     * Decodes the data from JSON format and overwrites current members data
     * This is usually as the first step when retrieving a blk from a peer
     * @param strJSONData data to be parsed
     * @return Encountered error (for diagnostics) or null if parsing was completed without errors
     */
    public String decodeJSON(String strJSONData){
        JSONObject jblk = null;

        try{
            jblk = new JSONObject(strJSONData);
        } catch (Exception ex) {
            return ("Error parsing JSON data! [" + ex.getMessage() + "]");
        }

        if (!jblk.has("BLK")) {
            return ("Error fetching data from JSON! [BLK is missing]");
        }

        JSONObject jobj = jblk.getJSONObject("BLK");

        if (!jobj.has("nonce")) {
            return ("Error fetching data from JSON! [nonce is missing]");
        }
        if (!jobj.has("sig")) {
            return ("Error fetching data from JSON! [sig is missing]");
        }
        if (!jobj.has("keys")) {
            return ("Error fetching data from JSON! [keys is missing]");
        }
        if (!jobj.has("txs")) {
            return ("Error fetching data from JSON! [txs is missing]");
        }
        if (!jobj.has("shard")) {
            return ("Error fetching data from JSON! [shard is missing]");
        }
        if (!jobj.has("ash")) {
            return ("Error fetching data from JSON! [ash is missing]");
        }
        if (!jobj.has("pbh")) {
            return ("Error fetching data from JSON! [ash is missing]");
        }

        try {
            BigInteger tempNonce = new BigInteger(jobj.getString("nonce"));
            String tempSig = jobj.getString("sig");
            JSONArray jsonArr = jobj.getJSONArray("keys");
            List<String> tempListPubKeys = new ArrayList<String>();
            for (int i = 0; i < jsonArr.length(); i++)
            {
                tempListPubKeys.add(jsonArr.getString(i));
            }

            jsonArr = jobj.getJSONArray("txs");
            List<byte[]> tempTxs = new ArrayList<byte[]>();
            for (int i = 0; i < jsonArr.length(); i++)
            {
                tempTxs.add(Base64.decode(jsonArr.getString(i)));
            }

            int tempShard = jobj.getInt("shard");
            byte[] tempAsh = Base64.decode(jobj.getString("ash"));

            byte[] tempPbh = Base64.decode(jobj.getString("pbh"));



            this.nonce = tempNonce;
            if (tempSig.length() == 0) {
                this.sig = null;
            } else {
              this.sig = Base64.decode(tempSig);
            }

            this.listPubKeys = tempListPubKeys;
            this.listTXHashes = tempTxs;
            this.shard = tempShard;
            this.appStateHash = tempAsh;
            this.prevBlockHash = tempPbh;

        } catch (Exception ex) {
            return ("Error fetching data from JSON! [something went horribly wrong converting data]");
        }

        return(null);
    }

}
