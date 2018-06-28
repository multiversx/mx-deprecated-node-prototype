package network.elrond.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import network.elrond.AsciiTable;
import network.elrond.sharding.Shard;

import java.beans.Transient;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * The Transaction class implements the transaction used for moving assets
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-11
 */
public class Transaction implements Serializable, AsciiPrintable {
    //tx counter
    private BigInteger nonce;
    //value used in transaction in sERDs see core.Util
    private BigInteger value;
    //receiving address as 0x0024f2849a...
    private String receiverAddress;
    //sender address as 0x0024f22323...
    private String senderAddress;
    //amount of sERDs per each unit of gas
    private BigInteger gasPrice;
    //gas used for running the tx
    private BigInteger gasLimit;
    //blob of data to executed in Elrond Virtual Machine
    private byte[] data;
    //blob of data containing first part of sig
    private byte[] signature;
    //blob of data containing second part of sig
    private byte[] challenge;
    //plain public key in hexa form
    private String pubKey;

    private Shard senderShard;

    private Shard receiverShard;

    /**
     * Default constructor
     */
    private Transaction() {
        nonce = BigInteger.ZERO;
        value = BigInteger.ZERO;
        receiverAddress = "";
        senderAddress = "";
        gasPrice = BigInteger.ZERO;
        gasLimit = BigInteger.ZERO;
        data = null;
        signature = null;
        challenge = null;
        pubKey = "";
    }

    /**
     * Explicit constructor
     *
     * @param nonce       tx counter
     * @param value       value used in transaction in sERDs @see core.Util
     * @param recvAddress receiving address as 0x0024f2849a...
     * @param sendAddress sender address as 0x0024f22323...
     */
    public Transaction(String sendAddress, String recvAddress, BigInteger value, BigInteger nonce, Shard senderShard, Shard receiverShard) {
        if (senderShard == null) {
            throw new IllegalArgumentException("SenderShard cannot be null");
        }

        if (receiverShard == null) {
            throw new IllegalArgumentException("ReceiverShard cannot be null");
        }

        if (sendAddress == null || sendAddress.isEmpty()) {
            throw new IllegalArgumentException("SendAddress cannot be null");
        }

        if (recvAddress == null || recvAddress.isEmpty()) {
            throw new IllegalArgumentException("RecvAddress cannot be null");
        }

        if (value == null || value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException(("Value cannot be lower than zero"));
        }

        if (nonce == null || nonce.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException(("Nonce cannot be lower than zero"));
        }


        this.nonce = nonce;
        this.value = value;
        this.receiverAddress = recvAddress;
        this.senderAddress = sendAddress;

        this.senderShard = senderShard;
        this.receiverShard = receiverShard;


        //free 4 all
        gasPrice = BigInteger.ZERO;
        gasLimit = BigInteger.ZERO;
        data = null;
        signature = null;
        challenge = null;
        pubKey = "";
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
     * Gets the value in sERDs
     *
     * @return value as BigInteger
     */
    public BigInteger getValue() {
        return (value);
    }

    /**
     * Sets the value in sERDs
     *
     * @param value to be set
     */
    public void setValue(BigInteger value) {
        this.value = value;
    }

    /**
     * Gets the receiving address as String : e.g. 0x37f345a....
     *
     * @return address as String
     */
    public String getReceiverAddress() {
        return (receiverAddress);
    }

    /**
     * Sets the receiving address
     *
     * @param receiverAddress to be set
     */
    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    /**
     * Gets the sender address as String : e.g. 0x37f345a....
     *
     * @return address as String
     */
    public String getSenderAddress() {
        return (senderAddress);
    }

    /**
     * Sets the sender address
     *
     * @param senderAddress to be set
     */
    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    /**
     * Gets the amount of sERDs for 1 unit of gas
     *
     * @return gas price as BigInteger
     */
    public BigInteger getGasPrice() {
        return (gasPrice);
    }

    /**
     * Sets the amount of sERDs for 1 unit of gas
     *
     * @param gasPrice to be set
     */
    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    /**
     * Gets the amount of gas available to run the tx
     *
     * @return gas limit as BigInteger
     */
    public BigInteger getGasLimit() {
        return (gasLimit);
    }

    /**
     * Sets the amount of gas available to run the tx
     *
     * @param gasLimit to be set
     */
    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    /**
     * Gets the RAW data to be executed by VM
     *
     * @return data as byte array
     */
    public byte[] getData() {
        return (data);
    }

    /**
     * Sets the RAW data to be executed by VM
     *
     * @param data to be set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the first part of signature of the tx
     *
     * @return sig as byte array
     */
    //@JsonProperty(SignatureSerializationType.TransactionSignature1)
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
    //@JsonProperty(SignatureSerializationType.TransactionSignature2)
    public byte[] getChallenge() {
        return (challenge);
    }

    /**
     * Sets the second part of signature of the tx
     *
     * @param challenge as byte array
     */
    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    /**
     * Gets the public key used for verifying the tx
     *
     * @return public key as String : e.g. 046f8a4352...
     */
    public String getPubKey() {
        return (pubKey);
    }

    /**
     * Sets the public key used for verifying the tx
     *
     * @param pubKey to be set
     */
    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public Shard getSenderShard() {
        return senderShard;
    }

    public void setSenderShard(Shard senderShard) {
        this.senderShard = senderShard;
    }

    public Shard getReceiverShard() {
        return receiverShard;
    }

    public void setReceiverShard(Shard receiverShard) {
        this.receiverShard = receiverShard;
    }

    @JsonIgnore
    @Transient
    public boolean isCrossShardTransaction() {
        return !receiverShard.equals(senderShard);
    }

    @Override
    public String toString() {
        return (String.format("Transaction{nonce=%d, value=%d, sender='%s', receiver='%s', senderShard='%s' ,receiverShard='%s' }",
                nonce, value, senderAddress, receiverAddress, senderShard, receiverShard));
    }

    @Override
    public AsciiTable print() {
        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(30);

        table.getColumns().add(new AsciiTable.Column("Transaction"));
        table.getColumns().add(new AsciiTable.Column(nonce + ""));

        AsciiTable.Row row0 = new AsciiTable.Row();
        row0.getValues().add("Value");
        row0.getValues().add(value.toString());

        AsciiTable.Row row1 = new AsciiTable.Row();
        row1.getValues().add("From");
        row1.getValues().add(senderAddress);
        table.getData().add(row1);

        AsciiTable.Row row2 = new AsciiTable.Row();
        row2.getValues().add("To");
        row2.getValues().add(receiverAddress);
        table.getData().add(row2);

        AsciiTable.Row row3 = new AsciiTable.Row();
        row3.getValues().add("From Shard");
        row3.getValues().add(senderShard.getIndex() + "");
        table.getData().add(row3);

        AsciiTable.Row row4 = new AsciiTable.Row();
        row4.getValues().add("To Shard");
        row4.getValues().add(receiverShard.getIndex() + "");
        table.getData().add(row4);

        AsciiTable.Row row5 = new AsciiTable.Row();
        row5.getValues().add("Nonce");
        row5.getValues().add(nonce.toString());
        table.getData().add(row5);


        table.calculateColumnWidth();
        return table;
    }
}
