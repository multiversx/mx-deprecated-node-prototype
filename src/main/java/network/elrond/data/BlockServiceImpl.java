package network.elrond.data;

import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The BlockServiceImpl class implements BlockService and is used to maintain Block objects
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-16
 */
public class BlockServiceImpl implements BlockService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Encodes in JSON format the block information using the data from all its fields
     * This is usually used when broadcasting the complete blk to peers
     *
     * @return JSON format of the blk as String
     */
    public String encodeJSON(Block blk, boolean withSig) {
        JSONObject jblk = new JSONObject();

        JSONObject jobj = new JSONObject();
        jobj.put("nonce", blk.getNonce().toString(10));

        if ((blk.getSig1() == null) || (!withSig)) {
            jobj.put("sig1", "");
        } else {
            jobj.put("sig1", new String(Base64.encode(blk.getSig1())));
        }

        if ((blk.getSig1() == null) || (!withSig)) {
            jobj.put("sig2", "");
        } else {
            jobj.put("sig2", new String(Base64.encode(blk.getSig2())));
        }

        //appends the public keys
        List<String> listPubKeys = blk.getListPublicKeys();
        for (int i = 0; i < listPubKeys.size(); i++) {
            jobj.append("keys", listPubKeys.get(i));
        }

        //appends the tx hashes
        List<byte[]> txHashes = blk.getListTXHashes();
        for (int i = 0; i < txHashes.size(); i++) {
            jobj.append("txs", new String(Base64.encode(txHashes.get(i))));
        }

        jobj.put("shard", blk.getShard());

        //prev block hash
        jobj.put("pbh", new String(Base64.encode(blk.getPrevBlockHash())));

        //app state hash
        jobj.put("ash", new String(Base64.encode(blk.getAppStateHash())));

        jblk.put("BLK", jobj);

        return (jblk.toString());
    }

     /**
     * Computes the hash of the block with an empty sig field
     * Used in signing/verifying process
     * @param blk block to be computed
     * @param withHash true, to include in hash the sig block (complete tx hash)
     * @return hash as byte array
     */
    public byte[] getHash(Block blk, boolean withHash) {
        return (Util.SHA3.digest(encodeJSON(blk, withHash).getBytes()));
    }

    /**
     * Computes the hash of the block with an empty sig field
     * Used in signing/verifying process
     * @param blk block to be computed
     * @param withHash true, to include in hash the sig block (complete tx hash)
     * @return hash as String
     */
    public String getHashAsString(Block blk, boolean withHash) {
        return (new String(Base64.encode(getHash(blk, true))));
    }

    /**
     * Decodes the data from JSON format
     * This is usually as the first step when retrieving a blk from a peer
     *
     * @param strJSONData data to be parsed
     * @return block from decoded JSON data, null if method encounters errors
     */
    public Block decodeJSON(String strJSONData) {
        JSONObject jblk = null;

        Block blk = new DataBlock();

        try {
            jblk = new JSONObject(strJSONData);
        } catch (Exception ex) {
            logger.error("Error parsing JSON data! [" + ex.getMessage() + "]");
            return(null);
        }

        if (!jblk.has("BLK")) {
            logger.error("Error fetching data from JSON! [BLK is missing]");
            return(null);
        }

        JSONObject jobj = jblk.getJSONObject("BLK");

        if (!jobj.has("nonce")) {
            logger.error("Error fetching data from JSON! [nonce is missing]");
            return(null);
        }
        if (!jobj.has("sig1")) {
            logger.error("Error fetching data from JSON! [sig1 is missing]");
            return(null);
        }
        if (!jobj.has("sig2")) {
            logger.error("Error fetching data from JSON! [sig2 is missing]");
            return(null);
        }
        if (!jobj.has("keys")) {
            logger.error("Error fetching data from JSON! [keys is missing]");
            return(null);
        }
        if (!jobj.has("txs")) {
            logger.error("Error fetching data from JSON! [txs is missing]");
            return(null);
        }
        if (!jobj.has("shard")) {
            logger.error("Error fetching data from JSON! [shard is missing]");
            return(null);
        }
        if (!jobj.has("ash")) {
            logger.error("Error fetching data from JSON! [ash is missing]");
            return(null);
        }
        if (!jobj.has("pbh")) {
            logger.error("Error fetching data from JSON! [ash is missing]");
            return(null);
        }

        try {
            BigInteger tempNonce = new BigInteger(jobj.getString("nonce"));
            String tempSig1 = jobj.getString("sig1");
            String tempSig2 = jobj.getString("sig2");
            JSONArray jsonArr = jobj.getJSONArray("keys");

            for (int i = 0; i < jsonArr.length(); i++) {
                blk.getListPublicKeys().add(jsonArr.getString(i));
            }

            jsonArr = jobj.getJSONArray("txs");
            for (int i = 0; i < jsonArr.length(); i++) {
                blk.getListTXHashes().add(Base64.decode(jsonArr.getString(i)));
            }

            int tempShard = jobj.getInt("shard");
            byte[] tempAsh = Base64.decode(jobj.getString("ash"));

            byte[] tempPbh = Base64.decode(jobj.getString("pbh"));

            if (Arrays.equals(tempPbh, GenesisBlock.STR_GENESIS_BLOCK.getBytes())){
                return(new GenesisBlock());
            }

            blk.setNonce(tempNonce);
            if (tempSig1.length() > 0) {
                blk.setSig1(Base64.decode(tempSig1));
            }
            if (tempSig2.length() > 0) {
                blk.setSig2(Base64.decode(tempSig2));
            }
            blk.setShard(tempShard);
            blk.setAppStateHash(tempAsh);
            blk.setPrevBlockHash(tempPbh);

        } catch (Exception ex) {
            logger.error("Error fetching data from JSON! [something went horribly wrong converting data]");
            return(null);
        }

        return (blk);
    }

    /**
     * Fetch transaction from memory pool (if they exists, otherwise get them from dht)
     * @param
     */
    public void solveBlocks(AppState appState) {
        Block[] blocksToBeChecked = (Block[])appState.syncDataBlk.getValues().toArray();

        Block blk;

        for (int i = 0; i < blocksToBeChecked.length; i++) {
            blk = blocksToBeChecked[i];

            if (blk.getIsSolved()) {
                continue;
            }

            //solving
            for (int j = 0; j < blk.getListTXHashes().size(); j++){
                //if tx has been solved, skip it
                if (blk.getListTransactions().get(j) != null){
                    continue;
                }

                byte[] hash = blk.getListTXHashes().get(j);

                blk.setTransaction(AppServiceProvider.getTransactionService().fetchTransaction(new String(Base64.encode(hash)), appState.syncDataTx), j);
            }
        }
    }
}
