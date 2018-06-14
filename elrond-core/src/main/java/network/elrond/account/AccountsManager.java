package network.elrond.account;

import network.elrond.core.Util;
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

    public Boolean hasFunds(Accounts accounts, String addressString, BigInteger value) throws IOException, ClassNotFoundException {
        Util.check(accounts != null, "accounts!=null");
        Util.check(!(addressString == null || addressString.isEmpty()), "addressString!=null");

        AccountAddress sendAddress = AccountAddress.fromHexString(addressString);
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(sendAddress, accounts);
        return senderAccountState.getBalance().compareTo(value) >= 0;
    }

    public Boolean hasCorrectNonce(Accounts accounts, String addressString, BigInteger nonce) throws IOException, ClassNotFoundException {
        Util.check(accounts != null, "accounts!=null");
        Util.check(!(addressString == null || addressString.isEmpty()), "addressString!=null");

        return true;
        //TODO: uncomment in the future
//        AccountAddress sendAddress = AccountAddress.fromHexaString(addressString);
//        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getOrCreateAccountState(sendAddress, accounts);
//        return senderAccountState.getNonce().equals(nonce);
    }

    public void transferFunds(Accounts accounts, String senderAddressString, String receiverAddressString, BigInteger value, BigInteger nonce) throws IOException, ClassNotFoundException {
        Util.check(accounts!=null, "accounts!=null");
        Util.check(!(senderAddressString == null || senderAddressString.isEmpty()), "senderAddressString!=null");
        Util.check(!(receiverAddressString == null || receiverAddressString.isEmpty()), "receiverAddressString!=null");
        Util.check(value.compareTo(BigInteger.ZERO) >= 0, "value>=0");
        Util.check(nonce.compareTo(BigInteger.ZERO) >= 0, "nonce>=0");

        if(!(hasFunds(accounts, senderAddressString, value) && hasCorrectNonce(accounts, senderAddressString, nonce))){
            throw new IllegalArgumentException("Validation of Sender Account failed!");
        }

        AccountAddress senderAddress = AccountAddress.fromHexString(senderAddressString);
        AccountAddress receiverAddress = AccountAddress.fromHexString(receiverAddressString);
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
