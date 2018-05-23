package network.elrond.data;

import java.math.BigInteger;

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
    private String receiverAddress;
    //sender address as 0x0024f22323...
    private String sendAddress;
    //amount of sERDs per each unit of gas
    private BigInteger gasPrice;
    //gas used for running the tx
    private BigInteger gasLimit;
    //blob of data to executed in Elrond Virtual Machine
    private byte[] data;
    //blob of data containing first part of sig
    private byte[] sig1;
    //blob of data containing second part of sig
    private byte[] sig2;
    //plain public key in hexa form
    private String pubKey;


    /**
     * Default constructor
     */
    public Transaction()
    {
        nonce = BigInteger.ZERO;
        value = BigInteger.ZERO;
        receiverAddress = "";
        sendAddress = "";
        gasPrice = BigInteger.ZERO;
        gasLimit = BigInteger.ZERO;
        data = null;
        sig1 = null;
        sig2 = null;
        pubKey = "";
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
        this.receiverAddress = recvAddress;
        this.sendAddress = sendAddress;
        //free 4 all
        gasPrice = BigInteger.ZERO;
        gasLimit = BigInteger.ZERO;
        data = null;
        sig1 = null;
        sig2 = null;
        pubKey = "";
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
    public String getReceiverAddress(){ return (receiverAddress); }

    /**
     * Sets the receiving address
     * @param receiverAddress to be set
     */
    public void setReceiverAddress(String receiverAddress) {this.receiverAddress = receiverAddress;}

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
     * Gets the public key used for verifying the tx
     * @return public key as String : e.g. 046f8a4352...
     */
    public String getPubKey(){return (pubKey);}

    /**
     * Sets the public key used for verifying the tx
     * @param pubKey to be set
     */
    public void setPubKey(String pubKey) {this.pubKey = pubKey;}

}
