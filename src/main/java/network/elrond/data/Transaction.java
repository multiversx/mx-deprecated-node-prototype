package network.elrond.data;

import java.math.BigInteger;

import network.elrond.core.Util;
import org.json.*;
import org.bouncycastle.util.encoders.Base64;

public class Transaction {
    //Tx counter
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
    private byte[] hash;

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
        hash = new byte[0];
    }

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
        hash = new byte[0];
    }

    public BigInteger getNonce() { return (nonce); }

    public void setNonce(BigInteger nonce){ this.nonce = nonce; }

    public BigInteger getValue() { return (value); }

    public void setValue(BigInteger value) {this.value = value;}

    public String getRecvAddress(){ return (recvAddress); }

    public void setRecvAddress(String recvAddress) {this.recvAddress = recvAddress;}

    public String getSendAddress(){ return (sendAddress); }

    public void setSendAddress(String sendAddress) {this.sendAddress = sendAddress;}

    public BigInteger getGasPrice(){return(gasPrice);}

    public void setGasPrice(BigInteger gasPrice) {this.gasPrice = gasPrice;}

    public BigInteger getGasLimit(){return (gasLimit);}

    public void setGasLimit(BigInteger gasLimit) {this.gasLimit = gasLimit;}

    public byte[] getData(){return(data);}

    public void setData(byte[] data){this.data = data;}

    public byte[] getSig(){return(sig);}

    public void setSig(byte[] sig){this.sig = sig;}

    public String getPublicKey(){return (pubKey);}

    public void setPubKey(String pubKey) {this.pubKey = pubKey;}

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
        jobj.put("key", pubKey);

        jtx.put("TX", jobj);

        return(jtx.toString());
    }

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
        jobj.put("key", pubKey);

        jtx.put("TX", jobj);

        return(jtx.toString());
    }

    //Loads the data into current object and outputs null if everything it's OK
    //outputs an error message string otherwise
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
            this.pubKey = tempKey;

        } catch (Exception ex) {
            return ("Error fetching data from JSON! [something went wrong converting data]");
        }

        return(null);
    }

    public byte[] getHash()
    {
        if (hash.length == 0)
        {
            //compute hash
            hash = Util.SHA3.digest(this.encodeJSONnoSig().getBytes());
        }

        return (hash);
    }

    public static Transaction createTransaction(String strJSONData) {
        Transaction tx = new Transaction();
        String result = tx.decodeJSON(strJSONData);

        //consistency test
        if (result != null) {
            return (null);
        }

        if ((tx.getNonce().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getValue().compareTo(BigInteger.ZERO) < 0) ||
                (tx.getSig().length == 0) ||
                (tx.sendAddress.length() != Util.MAX_LEN_ADDR) ||
                (tx.recvAddress.length() != Util.MAX_LEN_ADDR) ||
                (tx.getSig().length == 0) || //TO DO modify with actual value
                (tx.pubKey.length() != Util.MAX_LEN_PUB_KEY)
                ){
            return (null);
        }

        byte[] message = tx.getHash();




        return (null);
    }
}
