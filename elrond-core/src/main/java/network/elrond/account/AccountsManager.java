package network.elrond.account;

import network.elrond.data.AppBlockManager;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class AccountsManager {

    private Logger logger = LoggerFactory.getLogger(AppBlockManager.class);

    private static AccountsManager instance = new AccountsManager();

    public static AccountsManager instance() {
        return instance;
    }

    public Boolean HasFunds(Accounts accounts, String addressString, BigInteger value) throws IOException, ClassNotFoundException {
        if(accounts == null){
            throw new IllegalArgumentException("Accounts cannot be null");
        }
        if(addressString == null || addressString.isEmpty()){
            throw new IllegalArgumentException("AddressString cannot be null");
        }

        AccountAddress sendAddress = AccountAddress.fromHexaString(addressString);
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(sendAddress, accounts);
        return senderAccountState.getBalance().compareTo(value) >= 0;
    }

    public Boolean HasCorrectNonce(Accounts accounts, String addressString, BigInteger nonce) throws IOException, ClassNotFoundException {
        if(accounts == null){
            throw new IllegalArgumentException("Accounts cannot be null");
        }
        if(addressString == null || addressString.isEmpty()){
            throw new IllegalArgumentException("AddressString cannot be null");
        }

        AccountAddress sendAddress = AccountAddress.fromHexaString(addressString);
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(sendAddress, accounts);
        return senderAccountState.getNonce().equals(nonce);
    }

    public void TransferFunds(Accounts accounts, String senderAddressString, String receiverAddressString, BigInteger value, BigInteger nonce) throws IOException, ClassNotFoundException {

        if(accounts == null){
            throw new IllegalArgumentException("Accounts cannot be null");
        }
        if(senderAddressString == null || senderAddressString.isEmpty()){
            throw new IllegalArgumentException("SenderAddressString cannot be null");
        }
        if(receiverAddressString == null || receiverAddressString.isEmpty()){
            throw new IllegalArgumentException("ReceiverAddressString cannot be null");
        }
        if(value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }
        if(nonce.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Nonce cannot be negative");
        }

        if(!(HasFunds(accounts, senderAddressString, value) && HasCorrectNonce(accounts, senderAddressString, nonce))){
            throw new IllegalArgumentException("Validation of Sender Account failed!");
        }

        AccountAddress senderAddress = AccountAddress.fromHexaString(senderAddressString);
        AccountAddress receiverAddress = AccountAddress.fromHexaString(receiverAddressString);
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(senderAddress, accounts);
        AccountState receiverAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(receiverAddress, accounts);

        //transfer asset
        receiverAccountState.setBalance(receiverAccountState.getBalance().add(value));
        AppServiceProvider.getAccountStateService().setAccountState(receiverAddress, receiverAccountState, accounts); // PMS

        senderAccountState.setBalance(senderAccountState.getBalance().subtract(value));
        //increase sender nonce
        senderAccountState.setNonce(senderAccountState.getNonce().add(BigInteger.ONE));
        AppServiceProvider.getAccountStateService().setAccountState(senderAddress, senderAccountState, accounts); // PMS
    }
}
