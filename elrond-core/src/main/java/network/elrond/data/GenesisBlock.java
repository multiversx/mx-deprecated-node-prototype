package network.elrond.data;

import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * The GenesisBlock class extends Block and has HW fields
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public class GenesisBlock extends Block{
    private Transaction transactionMint;

    /**
     * Explicit constructor
     */
    public GenesisBlock(String strAddressMint, BigInteger startValue) {
        this.nonce = BigInteger.ZERO;
        hashNoSig = new byte[0];
        hash = new byte[0];
        listPubKeys = new ArrayList<String>();
        prevBlockHash = new byte[0];
        listTXHashes = new ArrayList<byte[]>();

        transactionMint = new Transaction();
        transactionMint.setNonce(BigInteger.ZERO);
        transactionMint.setSendAddress(Util.getAddressFromPublicKey(Util.PUBLIC_KEY_MINTING.getValue()));
        transactionMint.setReceiverAddress(strAddressMint);
        transactionMint.setPubKey(Util.byteArrayToHexString(Util.PUBLIC_KEY_MINTING.getValue()));
        if (startValue.compareTo(Util.VALUE_MINTING) > 0){
            transactionMint.setValue(Util.VALUE_MINTING);
        } else {
            transactionMint.setValue(startValue);
        }
        AppServiceProvider.getTransactionService().signTransaction(transactionMint, Util.PRIVATE_KEY_MINTING.getValue());

        this.listTXHashes.add(AppServiceProvider.getSerializationService().getHash(transactionMint, true));

        shard = 0;
    }

    public Transaction getTransactionMint(){ return transactionMint;}
}