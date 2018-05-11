package network.elrond.data;

import java.math.BigInteger;
import org.json.*;
import org.bouncycastle.util.encoders.Base64;
import network.elrond.core.Util;

/**
 * The Transaction class implements the transaction used for moving assets
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-11
 */
public class Transaction {
    //tx counter
    private BigInteger nonce;
    //value used in transaction in sERDs see core.Util
    private BigInteger value;
    //receiving address as 0x0024f2849a...
    private String recvAddress;
    //sender address as 0x0024f22323...
    private String sendAddress;
    //amount of sERDs per each unit of gas
    private BigInteger gasPrice;
    //gas used for running the tx
    private BigInteger gasLimit;
    //blob of data to executed in Elrond Virtual Machine
    private byte[] data;
    //blob of data containing sig
    private byte[] sig;
    //plain public key in hexa form
    private String pubKey;
    //plain message hash
    private byte[] hashNoSig;
    //complete tx hash
    private byte[] hash;

    /**
     * Default constructor
     */
    public Transaction()
    {
        nonce = BigInteger.ZERO;
        value = BigInteger.ZERO;
        recvAddress = "";
        sendAddress = "";
        gasPrice = BigInteger.ZERO;
        gasLimit = BigInteger.ZERO;
        data = null;
        sig = null;
        pubKey = "";
        hashNoSig = new byte[0];
        hash = new byte[0];
    }

    /**
     * Explicit constructor
     * @param nonce tx counter
     * @param value value used in transaction in sERDs @see core.Util
     * @param recvAddress receiving address as 0x0024f2849a...
     * @param sendAddress sender address as 0x0024f22323...
     */
    public Transaction(BigInteger nonce, BigInteger value, String recvAddress, String sendAddress)
    {
        this.nonce = nonce;
        this.value = value;
        this.recvAddress = recvAddress;
        this.sendAddress = sendAddress;
        //free 4 all
        gasPrice = BigInteger.ZERO;
        gasLimit = BigInteger.ZERO;
        data = null;
        sig = null;
        pubKey = "";
        hashNoSig = new byte[0];
        hash = new byte[0];
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
    public void setNonce(BigInteger nonce){ this.nonce = nonce; }

    /**
     * Gets the value in sERDs
     * @return value as BigInteger
     */
    public BigInteger getValue() { return (value); }

    /**
     * Sets the value in sERDs
     * @param value to be set
     */
    public void setValue(BigInteger value) {this.value = value;}

    /**
     * Gets the receiving address as String : e.g. 0x37f345a....
     * @return address as String
     */
    public String getRecvAddress(){ return (recvAddress); }

    /**
     * Sets the receiving address
     * @param recvAddress to be set
     */
    public void setRecvAddress(String recvAddress) {this.recvAddress = recvAddress;}

    /**
     * Gets the sender address as String : e.g. 0x37f345a....
     * @return address as String
     */
    public String getSendAddress(){ return (sendAddress); }

    /**
     * Sets the sender address
     * @param sendAddress to be set
     */
    public void setSendAddress(String sendAddress) {this.sendAddress = sendAddress;}

    /**
     * Gets the amount of sERDs for 1 unit of gas
     * @return gas price as BigInteger
     */
    public BigInteger getGasPrice(){return(gasPrice);}

    /**
     * Sets the amount of sERDs for 1 unit of gas
     * @param gasPrice to be set
     */
    public void setGasPrice(BigInteger gasPrice) {this.gasPrice = gasPrice;}

    /**
     * Gets the amount of gas available to run the tx
     * @return gas limit as BigInteger
     */
    public BigInteger getGasLimit(){return (gasLimit);}

    /**
     * Sets the amount of gas available to run the tx
     * @param gasLimit to be set
     */
    public void setGasLimit(BigInteger gasLimit) {this.gasLimit = gasLimit;}

    /**
     * Gets the RAW data to be executed by VM
     * @return data as byte array
     */
    public byte[] getData(){return(data);}

    /**
     * Sets the RAW data to be executed by VM
     * @param data to be set
     */
    public void setData(byte[] data){this.data = data;}

    /**
     * Gets the signature of the tx
     * @return sig as byte array
     */
    public byte[] getSig(){return(sig);}

    /**
     * Sets the signature of the tx
     * @param sig as byte array
     */
    public void setSig(byte[] sig){this.sig = sig;}

    /**
     * Gets the public key used for verifying the tx
     * @return public key as String : e.g. 046f8a4352...
     */
    public String getPublicKey(){return (pubKey);}

    /**
     * Sets the public key used for verifying the tx
     * @param pubKey to be set
     */
    public void setPubKey(String pubKey) {this.pubKey = pubKey;}

    /**
     * Encodes in JSON format the tx information using sig field as empty string ""
     * This is usually used to generate the hash without signature for signing/verifying the tx
     * @return JSON format of the tx as String
     */
    public String encodeJSONnoSig() {
        JSONObject jtx = new JSONObject();

        JSONObject jobj = new JSONObject();
        jobj.put("nonce", nonce.toString(10));
        jobj.put("value", value.toString(10));
        jobj.put("rcv", recvAddress);
        jobj.put("snd", sendAddress);
        jobj.put("gprice", gasPrice.toString(10));
        jobj.put("glimit", gasPrice.toString(10));
        if (data == null) {
            jobj.put("data", "");
        } else {
            jobj.put("data", new String(Base64.encode(data)));
        }
        jobj.put("sig", "");
        //hexa form -> byte array -> base64 (reduce the size)
        jobj.put("key", new String(Base64.encode(Util.hexStringToByteArray(pubKey))));

        jtx.put("TX", jobj);

        return(jtx.toString());
    }

    /**
     * Encodes in JSON format the tx information using the data from all its fields
     * This is usually used when broadcasting the complete tx to peers
     * @return JSON format of the tx as String
     */
    public String encodeJSON() {
        JSONObject jtx = new JSONObject();

        JSONObject jobj = new JSONObject();
        jobj.put("nonce", nonce.toString(10));
        jobj.put("value", value.toString(10));
        jobj.put("rcv", recvAddress);
        jobj.put("snd", sendAddress);
        jobj.put("gprice", gasPrice.toString(10));
        jobj.put("glimit", gasPrice.toString(10));
        if (data == null) {
            jobj.put("data", "");
        } else {
            jobj.put("data", new String(Base64.encode(data)));
        }
        if (sig == null)
        {
            jobj.put("sig", "");
        } else {
            jobj.put("sig", new String(Base64.encode(sig)));
        }
        //hexa form -> byte array -> base64 (reduce the size)
        jobj.put("key", new String(Base64.encode(Util.hexStringToByteArray(pubKey))));

        jtx.put("TX", jobj);

        return(jtx.toString());
    }

    /**
     * Decodes the data from JSON format and overwrites current members data
     * This is usually as the first step when retrieving a tx from a peer
     * @param strJSONData data to be parsed
     * @return Encountered error (for diagnostics) or null if parsing was completed without errors
     */
    public String decodeJSON(String strJSONData){
        JSONObject jtx = null;

        try{
            jtx = new JSONObject(strJSONData);
        } catch (Exception ex) {
            return ("Error parsing JSON data! [" + ex.getMessage() + "]");
        }

        if (!jtx.has("TX")) {
            return ("Error fetching data from JSON! [TX is missing]");
        }

        JSONObject jobj = jtx.getJSONObject("TX");

        if (!jobj.has("nonce")) {
            return ("Error fetching data from JSON! [nonce is missing]");
        }
        if (!jobj.has("value")) {
            return ("Error fetching data from JSON! [value is missing]");
        }
        if (!jobj.has("rcv")) {
            return ("Error fetching data from JSON! [rcv is missing]");
        }
        if (!jobj.has("snd")) {
            return ("Error fetching data from JSON! [snd is missing]");
        }
        if (!jobj.has("gprice")) {
            return ("Error fetching data from JSON! [gprice is missing]");
        }
        if (!jobj.has("glimit")) {
            return ("Error fetching data from JSON! [glimit is missing]");
        }
        if (!jobj.has("data")) {
            return ("Error fetching data from JSON! [data is missing]");
        }
        if (!jobj.has("sig")) {
            return ("Error fetching data from JSON! [sig is missing]");
        }
        if (!jobj.has("key")) {
            return ("Error fetching data from JSON! [(public) key is missing]");
        }

        try {
            BigInteger tempNonce = new BigInteger(jobj.getString("nonce"));
            BigInteger tempValue = new BigInteger(jobj.getString("value"));
            String tempRecv = jobj.getString("rcv");
            String tempSend = jobj.getString("snd");
            BigInteger tempGPrice = new BigInteger(jobj.getString("gprice"));
            BigInteger tempGLimit = new BigInteger(jobj.getString("glimit"));
            String tempData = jobj.getString("data");
            String tempSig = jobj.getString("sig");
            String tempKey = jobj.getString("key");

            if (tempData.equals("")){
                tempData = null;
            }

            this.nonce = tempNonce;
            this.value = tempValue;
            this.recvAddress = tempRecv;
            this.sendAddress = tempSend;
            this.gasLimit = tempGLimit;
            this.gasPrice = tempGPrice;
            this.data = Base64.decode(tempData.getBytes());
            this.sig = Base64.decode(tempSig.getBytes());
            this.pubKey = Util.byteArrayToHexString(Base64.decode(tempKey));

        } catch (Exception ex) {
            return ("Error fetching data from JSON! [something went horribly wrong converting data]");
        }

        return(null);
    }

    /**
     * Computes the hash of the tx with an empty sig field
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
     * Used as a mean of tx identification
     * @return hash as byte array
     */
    public byte[] getHash()
    {
        if (hash.length == 0)
        {
            //compute hash
            hash = Util.SHA3.digest(this.encodeJSON().getBytes());
        }

        return (hash);
    }

    /**
     * TO DO
     * @param privateKeysBytes
     */
    public void signTransaction(byte[] privateKeysBytes) {
        //TO DO
    }

    /**
     * Verify the data stored in tx
     * @param tx to be verified
     * @return true if tx passes all consistency tests
     */
    public static Boolean verifyTransaction(Transaction tx) {
        //test 1. consistency checks
        if ((tx.getNonce().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getValue().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getSig().length == 0) ||
                (tx.sendAddress.length() != Util.MAX_LEN_ADDR) ||
                (tx.recvAddress.length() != Util.MAX_LEN_ADDR) ||
                (tx.getSig().length == 0) || //TO DO modify with actual value
                (tx.pubKey.length() != Util.MAX_LEN_PUB_KEY * 2)
                ){
            return (false);
        }

        //test 2. verify if sender address is generated from public key used to sign tx
        if (!tx.getSendAddress().equals(Util.getAddressFromPublicKey(Util.hexStringToByteArray(tx.pubKey)))) {
            return (false);
        }

        //test 3. verify the signature
        byte[] message = tx.getHashNoSig();
        //TO DO

        return (true);
    }

    /**
     * Recreates a tx from JSON format string and checks the consistency
     * @param strJSONData to be decoded
     * @return the new tx or null if something went wrong in decoding/parsing/checking processes
     */
    public static Transaction createTransaction(String strJSONData) {
        Transaction tx = new Transaction();
        String result = tx.decodeJSON(strJSONData);

        //consistency test
        if (result != null) {
            return (null);
        }

        if (!verifyTransaction(tx)) {
            return(null);
        }


        //test if the sender address is derived from the public that signed the sig block



        return (tx);
    }
}
