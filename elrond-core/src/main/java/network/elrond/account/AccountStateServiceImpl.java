package network.elrond.account;

import network.elrond.core.RLP;
import network.elrond.core.RLPList;
import network.elrond.core.Util;
import network.elrond.data.ExecutionReport;
import network.elrond.data.ExecutionService;
import network.elrond.data.GenesisBlock;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.math.BigInteger;

public class AccountStateServiceImpl implements AccountStateService {

//    public byte[] getHash(AccountState state) {
//        String json = AppServiceProvider.getSerializationService().encodeJSON(state);
//        return (Util.SHA3.digest(json.getBytes()));
//    }

    @Override
    public synchronized AccountState getOrCreateAccountState(AccountAddress address, Accounts accounts) throws IOException, ClassNotFoundException {
        AccountState state = getAccountState(address, accounts);

        if (state != null) return state;

        setAccountState(address, new AccountState(), accounts);
        return getAccountState(address, accounts);
    }

    @Override
    public synchronized AccountState getAccountState(AccountAddress address, Accounts accounts) {

        if (address == null) {
            return null;
        }

        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        byte[] bytes = address.getBytes();
        return (bytes != null) ? convertToAccountStateFromRLP(unit.get(bytes)) : null;

    }

    @Override
    public synchronized void rollbackAccountStates(Accounts accounts) {
        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.rollBack();

    }

    @Override
    public synchronized void commitAccountStates(Accounts accounts) {
        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.commit();
    }

    @Override
    public synchronized void setAccountState(AccountAddress address, AccountState state, Accounts accounts) {

        if (address == null || state == null) {
            return;
        }

        AccountsPersistenceUnit<AccountAddress, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.put(address.getBytes(), convertAccountStateToRLP(state));

    }

    @Override
    public byte[] convertAccountStateToRLP(AccountState accountState) {
        byte[] nonce = RLP.encodeBigInteger(accountState.getNonce());
        byte[] balance = RLP.encodeBigInteger(accountState.getBalance());

        return RLP.encodeList(nonce, balance);
    }


    @Override
    public AccountState convertToAccountStateFromRLP(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        AccountState accountState = new AccountState();
        RLPList items = (RLPList) RLP.decode2(data).get(0);
        accountState.setNonce(new BigInteger(1, ((items.get(0).getRLPData()) == null ? new byte[]{0} :
                items.get(0).getRLPData())));
        accountState.setBalance(new BigInteger(1, ((items.get(1).getRLPData()) == null ? new byte[]{0} :
                items.get(1).getRLPData())));

        return (accountState);
    }

    public void initialMintingToKnownAddress(Accounts accounts){

        AccountState accountState = null;

        try {
            accountState = getOrCreateAccountState(AccountAddress.fromPublicKey(Util.PUBLIC_KEY_MINTING), accounts);
            accountState.setBalance(Util.VALUE_MINTING);
            setAccountState(AccountAddress.fromPublicKey(Util.PUBLIC_KEY_MINTING), accountState, accounts);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public GenesisBlock generateGenesisBlock(String strAddressMint, BigInteger startValue, AccountsContext accountsContextTemporary){
        GenesisBlock gb = new GenesisBlock(strAddressMint, startValue);

        //compute state root hash
        try {
            Accounts accountsTemp = new Accounts(accountsContextTemporary);
            ExecutionService executionService = AppServiceProvider.getExecutionService();
            ExecutionReport executionReport = executionService.processTransaction(gb.getTransactionMint(), accountsTemp);
            if (!executionReport.isOk()){
                return(null);
            }

            gb.setAppStateHash(accountsTemp.getAccountsPersistenceUnit().getRootHash());
            accountsTemp.getAccountsPersistenceUnit().close();
        } catch (Exception ex){
            ex.printStackTrace();
            return(null);
        }

        return (gb);
    }
}