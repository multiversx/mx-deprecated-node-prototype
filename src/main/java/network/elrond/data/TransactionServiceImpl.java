package network.elrond.data;

import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.crypto.Signature;
import network.elrond.crypto.SignatureService;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * The TransactionServiceImpl class implements TransactionService and is used to maintain Transaction objects
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-16
 */
public class TransactionServiceImpl implements TransactionService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Encodes in JSON format the tx information using the data from all its fields
     * This is usually used when broadcasting the complete tx to peers
     *
     * @param tx      transaction
     * @param withSig whether or not to include the signature parts in output String
     * @return JSON format of the tx as String
     */
    public String encodeJSON(Transaction tx, boolean withSig) {
        JSONObject jtx = new JSONObject();

        JSONObject jobj = new JSONObject();
        jobj.put("nonce", tx.getNonce().toString(10));
        jobj.put("value", tx.getValue().toString(10));
        jobj.put("rcv", tx.getRecvAddress());
        jobj.put("snd", tx.getSendAddress());
        jobj.put("gprice", tx.getGasPrice().toString(10));
        jobj.put("glimit", tx.getGasLimit().toString(10));
        if (tx.getData() == null) {
            jobj.put("data", "");
        } else {
            jobj.put("data", new String(Base64.encode(tx.getData())));
        }
        if ((tx.getSig1() == null) || (!withSig)) {
            jobj.put("sig1", "");
        } else {
            jobj.put("sig1", new String(Base64.encode(tx.getSig1())));
        }
        if ((tx.getSig2() == null) || (!withSig)) {
            jobj.put("sig2", "");
        } else {
            jobj.put("sig2", new String(Base64.encode(tx.getSig2())));
        }
        //hexa form -> byte array -> base64 (reduce the size)
        jobj.put("key", new String(Base64.encode(Util.hexStringToByteArray(tx.getPublicKey()))));

        jtx.put("TX", jobj);

        return (jtx.toString());
    }

    /**
     * Decodes the data from JSON format
     * This is usually as the first step when retrieving a tx from a peer
     *
     * @param strJSONData data to be parsed
     * @return transaction from decoded JSON data, null if method encounters errors
     */
    public Transaction decodeJSON(String strJSONData) {
        JSONObject jtx = null;

        Transaction tx = new Transaction();

        try {
            jtx = new JSONObject(strJSONData);
        } catch (Exception ex) {
            logger.error("Error parsing JSON data! [" + ex.getMessage() + "]");
            return (null);
        }

        if (!jtx.has("TX")) {
            logger.error("Error fetching data from JSON! [TX is missing]");
            return (null);
        }

        JSONObject jobj = jtx.getJSONObject("TX");

        if (!jobj.has("nonce")) {
            logger.error("Error fetching data from JSON! [nonce is missing]");
            return (null);
        }
        if (!jobj.has("value")) {
            logger.error("Error fetching data from JSON! [value is missing]");
            return (null);
        }
        if (!jobj.has("rcv")) {
            logger.error("Error fetching data from JSON! [rcv is missing]");
            return (null);
        }
        if (!jobj.has("snd")) {
            logger.error("Error fetching data from JSON! [snd is missing]");
            return (null);
        }
        if (!jobj.has("gprice")) {
            logger.error("Error fetching data from JSON! [gprice is missing]");
            return (null);
        }
        if (!jobj.has("glimit")) {
            logger.error("Error fetching data from JSON! [glimit is missing]");
            return (null);
        }
        if (!jobj.has("data")) {
            logger.error("Error fetching data from JSON! [data is missing]");
            return (null);
        }
        if (!jobj.has("sig1")) {
            logger.error("Error fetching data from JSON! [sig1 is missing]");
            return (null);
        }
        if (!jobj.has("sig2")) {
            logger.error("Error fetching data from JSON! [sig2 is missing]");
            return (null);
        }
        if (!jobj.has("key")) {
            logger.error("Error fetching data from JSON! [(public) key is missing]");
            return (null);
        }

        try {
            BigInteger tempNonce = new BigInteger(jobj.getString("nonce"));
            BigInteger tempValue = new BigInteger(jobj.getString("value"));
            String tempRecv = jobj.getString("rcv");
            String tempSend = jobj.getString("snd");
            BigInteger tempGPrice = new BigInteger(jobj.getString("gprice"));
            BigInteger tempGLimit = new BigInteger(jobj.getString("glimit"));
            String tempData = jobj.getString("data");
            String tempSig1 = jobj.getString("sig1");
            String tempSig2 = jobj.getString("sig2");
            String tempKey = jobj.getString("key");

            if (tempData.equals("")) {
                tempData = null;
            }

            tx.setNonce(tempNonce);
            tx.setValue(tempValue);
            tx.setRecvAddress(tempRecv);
            tx.setSendAddress(tempSend);
            tx.setGasLimit(tempGLimit);
            tx.setGasPrice(tempGPrice);
            tx.setData(Base64.decode(tempData.getBytes()));
            if ((tempSig1 != null) && (tempSig1.length() > 0)) {
                tx.setSig1(Base64.decode(tempSig1.getBytes()));
            }
            if ((tempSig2 != null) && (tempSig2.length() > 0)) {
                tx.setSig2(Base64.decode(tempSig2.getBytes()));
            }
            tx.setPubKey(Util.byteArrayToHexString(Base64.decode(tempKey)));

        } catch (Exception ex) {
            logger.error("Error fetching data from JSON! [something went horribly wrong converting data]");
            return (null);
        }

        return (tx);
    }

    /**
     * Computes the hash of the complete tx info
     * Used as a mean of tx identification
     *
     * @param tx      transaction
     * @param withSig whether or not to include the signature parts in hash
     * @return hash as byte array
     */
    public byte[] getHash(Transaction tx, boolean withSig) {
        return (Util.SHA3.digest(encodeJSON(tx, withSig).getBytes()));
    }

    /**
     * Signs the transaction using private keys
     *
     * @param tx               transaction
     * @param privateKeysBytes private key as byte array
     */
    public void signTransaction(Transaction tx, byte[] privateKeysBytes) {
        PrivateKey pvkey = new PrivateKey(privateKeysBytes);
        byte[] hashNoSigLocal = getHash(tx, false);
        PublicKey pbkey = new PublicKey(pvkey);
        Signature sig;

        SignatureService schnorr = AppServiceProvider.getSignatureService();
        sig = schnorr.signMessage(hashNoSigLocal, pvkey, pbkey);

        tx.setSig1(sig.getSignature());
        tx.setSig2(sig.getChallenge());
    }

    /**
     * Verify the data stored in tx
     *
     * @param tx to be verified
     * @return true if tx passes all consistency tests
     */
    public boolean verifyTransaction(Transaction tx) {
        //test 1. consistency checks
        if ((tx.getNonce().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getValue().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getSig1() == null) ||
                (tx.getSig2() == null) ||
                (tx.getSig1().length == 0) ||
                (tx.getSig2().length == 0) ||
                (tx.getSendAddress().length() != Util.MAX_LEN_ADDR) ||
                (tx.getRecvAddress().length() != Util.MAX_LEN_ADDR) ||
                (tx.getPublicKey().length() != Util.MAX_LEN_PUB_KEY * 2)
                ) {
            return (false);
        }

        //test 2. verify if sender address is generated from public key used to sign tx
        if (!tx.getSendAddress().equals(Util.getAddressFromPublicKey(Util.hexStringToByteArray(tx.getPublicKey())))) {
            return (false);
        }

        //test 3. verify the signature
        byte[] message = getHash(tx, false);
        SignatureService schnorr = AppServiceProvider.getSignatureService();
        Signature sig = new Signature();
        if ((tx.getSig1() != null) && (tx.getSig1().length > 0) &&
                (tx.getSig2() != null) && (tx.getSig2().length > 0)) {
            sig.setSignature(tx.getSig1());
            sig.setChallenge(tx.getSig2());
        }

        PublicKey pbKey = new PublicKey();
        try {
            pbKey.setPublicKey(Util.hexStringToByteArray(tx.getPublicKey()));
        } catch (Exception ex) {
            return (false);
        }

        if (!schnorr.verifySignature(sig.getSignature(), sig.getChallenge(), message, pbKey)) {
            return (false);
        }

        return (true);
    }
}