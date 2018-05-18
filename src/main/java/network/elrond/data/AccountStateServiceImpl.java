package network.elrond.data;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccountStateServiceImpl implements AccountStateService{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ConcurrentHashMap<String, AccountState> TEST_ACCOUNT_STATES = new ConcurrentHashMap<>();
    private List<AccountState> listAccumulatedAccounts = new ArrayList<>();
    private List<String> listAccumulatedAddresses = new ArrayList<>();

    public String encodeJSON(AccountState accountState){
        JSONObject jas = new JSONObject();

        JSONObject jobj = new JSONObject();
        jobj.put("nonce", accountState.getNonce().toString(10));
        jobj.put("balance", accountState.getBalance().toString(10));
        jobj.put("locked", accountState.getValidatorLockedStake().toString(10));
        jobj.put("reputation", accountState.getValidatorReputation());
        jobj.put("shard", accountState.getValidatorShardNo());

        jas.put("AS", jobj);

        return (jas.toString());
    }

    public AccountState decodeJSON(String strJSONData){
        JSONObject jas = null;

        AccountState accountState = new AccountState();

        try {
            jas = new JSONObject(strJSONData);
        } catch (Exception ex) {
            logger.error("Error parsing JSON data! [" + ex.getMessage() + "]");
            return (null);
        }

        if (!jas.has("AS")) {
            logger.error("Error fetching data from JSON! [AS is missing]");
            return (null);
        }

        JSONObject jobj = jas.getJSONObject("AS");

        if (!jobj.has("nonce")) {
            logger.error("Error fetching data from JSON! [nonce is missing]");
            return (null);
        }
        if (!jobj.has("balance")) {
            logger.error("Error fetching data from JSON! [balance is missing]");
            return (null);
        }
        if (!jobj.has("locked")) {
            logger.error("Error fetching data from JSON! [locked is missing]");
            return (null);
        }
        if (!jobj.has("reputation")) {
            logger.error("Error fetching data from JSON! [reputation is missing]");
            return (null);
        }
        if (!jobj.has("shard")) {
            logger.error("Error fetching data from JSON! [shard is missing]");
            return (null);
        }

        try {
            BigInteger tempNonce = new BigInteger(jobj.getString("nonce"));
            BigInteger tempBalance = new BigInteger(jobj.getString("balance"));
            BigInteger tempLocked = new BigInteger(jobj.getString("locked"));
            int tempReputation = jobj.getInt("reputation");
            int tempShard = jobj.getInt("shard");

            accountState.setNonce(tempNonce);
            accountState.setBalance(tempBalance);
            accountState.setValidatorLockedStake(tempLocked);
            accountState.setValidatorReputation(tempReputation);
            accountState.setValidatorShardNo(tempShard);

        } catch (Exception ex) {
            logger.error("Error fetching data from JSON! [something went horribly wrong converting data]");
            return (null);
        }

        return (accountState);
    }

    public byte[] getHash(AccountState accountState){
        return (Util.SHA3.digest(encodeJSON(accountState).getBytes()));
    }

    public AccountState getCreateAccount(String strAdress){
        //TO DO : search for account and return if exists
        //for testing purposes, I used a ConcurentMap object

        if (TEST_ACCOUNT_STATES.containsKey(strAdress))
        {
            return (TEST_ACCOUNT_STATES.get(strAdress));
        }

        AccountState accountState = new AccountState();
        TEST_ACCOUNT_STATES.put(strAdress, accountState);

        return (accountState);
    }

    public void updateAccount(String strAddress, AccountState accountState){
        if (accountState == null){
            accountState = new AccountState();
        }

        TEST_ACCOUNT_STATES.put(strAddress, accountState);
    }

    //array with newly created accounts (to sandbox the execution)
    //first = receiver account, second = sender account
    public AccountState[] executeTransaction(Transaction tx) throws Exception{
        if (tx == null){
            throw new Exception("NULL transaction object!");
        }

        TransactionService txServ = AppServiceProvider.getTransactionService();

        String strHash = new String(Base64.encode(txServ.getHash(tx, true)));

        if (!txServ.verifyTransaction(tx)){
            throw new Exception("Invalid transaction! tx hash: " + strHash);
        }

        //We have to copy-construct the objects for sandbox mode
        AccountState asRecv = new AccountState(getCreateAccount(tx.getRecvAddress()));
        AccountState asSend = new AccountState(getCreateAccount(tx.getSendAddress()));

        if (asSend.getBalance().compareTo(tx.getValue()) < 0){
            throw new Exception("Invalid transaction! Will result in negative balance! tx hash: " + strHash);
        }

        if (!asSend.getNonce().equals(tx.getNonce())){
            throw new Exception("Invalid transaction! Nonce mismatch! tx hash: " + strHash);
        }

        //transfer asset
        asRecv.setBalance(asRecv.getBalance().add(tx.getValue()));
        asSend.setBalance(asSend.getBalance().subtract(tx.getValue()));
        //increase sender nonce
        asSend.setNonce(asSend.getNonce().add(BigInteger.ONE));

        return (new AccountState[]{asRecv, asSend});
    }

    public void doRollBackLastAccumulatedData(){
        listAccumulatedAccounts.clear();
        listAccumulatedAddresses.clear();
    }

    public void doCommitLastAccumulatedData(){
        for (int i = 0; i < listAccumulatedAccounts.size(); i++){
            updateAccount(listAccumulatedAddresses.get(i), listAccumulatedAccounts.get(i));
        }
    }

    public void executeTransactionAccumulatingData(Transaction tx) throws Exception{
        AccountState[] result = executeTransaction(tx);

        String strRecv = tx.getRecvAddress();
        String strSend = tx.getSendAddress();

        int idxRecv = listAccumulatedAddresses.indexOf(strRecv);
        int idxSend = listAccumulatedAddresses.indexOf(strSend);

        if (idxRecv > 0){
            listAccumulatedAccounts.set(idxRecv, result[0]);
        } else {
            listAccumulatedAddresses.add(strRecv);
            listAccumulatedAccounts.add(result[0]);
        }

        if (idxSend > 0){
            listAccumulatedAccounts.set(idxSend, result[1]);
        } else {
            listAccumulatedAddresses.add(strSend);
            listAccumulatedAccounts.add(result[1]);
        }
    }


}
