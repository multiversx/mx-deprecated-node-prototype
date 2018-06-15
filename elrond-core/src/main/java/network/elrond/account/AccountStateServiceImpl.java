package network.elrond.account;

import network.elrond.chronology.NTPClient;
import network.elrond.core.RLP;
import network.elrond.core.RLPList;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.service.AppServiceProvider;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigInteger;

public class AccountStateServiceImpl implements AccountStateService {


    @Override
    public synchronized AccountState getOrCreateAccountState(AccountAddress address, Accounts accounts) throws IOException, ClassNotFoundException {
        AccountState state = getAccountState(address, accounts);

        if (state != null) {
            return state;
        }

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
        unit.getCache().put(address, state);
        accounts.getAddresses().add(address);

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

    public void initialMintingToKnownAddress(Accounts accounts) {

        AccountState accountState = null;

        try {
            accountState = getOrCreateAccountState(AccountAddress.fromPublicKey(Util.PUBLIC_KEY_MINTING), accounts);
            accountState.setBalance(Util.VALUE_MINTING);
            setAccountState(AccountAddress.fromPublicKey(Util.PUBLIC_KEY_MINTING), accountState, accounts);
            accounts.getAccountsPersistenceUnit().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Fun.Tuple2<Block, Transaction> generateGenesisBlock(String initialAddress, BigInteger initialValue,
                                                               AccountsContext accountsContextTemporary, PrivateKey privateKey,
                                                               NTPClient ntpClient) {

        if (initialValue.compareTo(Util.VALUE_MINTING) > 0) {
            initialValue = Util.VALUE_MINTING;
        }


        Transaction transactionMint = AppServiceProvider.getTransactionService().generateTransaction(Util.PUBLIC_KEY_MINTING,
                new PublicKey(Util.hexStringToByteArray(initialAddress)), initialValue, BigInteger.ZERO);
        AppServiceProvider.getTransactionService().signTransaction(transactionMint, Util.PRIVATE_KEY_MINTING.getValue(), Util.PUBLIC_KEY_MINTING.getValue());

        Block genesisBlock = new Block();
        genesisBlock.setNonce(BigInteger.ZERO);
        genesisBlock.getListTXHashes().add(AppServiceProvider.getSerializationService().getHash(transactionMint));
        //set the timestamp & round
        genesisBlock.setTimestamp(AppServiceProvider.getChronologyService().getSynchronizedTime(ntpClient));
        genesisBlock.setRoundHeight(0);

        //compute state root hash
        try {
            Accounts accountsTemp = new Accounts(accountsContextTemporary);

            ExecutionService executionService = AppServiceProvider.getExecutionService();
            ExecutionReport executionReport = executionService.processTransaction(transactionMint, accountsTemp);
            if (!executionReport.isOk()) {
                return (null);
            }
            genesisBlock.setAppStateHash(accountsTemp.getAccountsPersistenceUnit().getRootHash());
            AppBlockManager.instance().signBlock(genesisBlock, privateKey);
            accountsTemp.getAccountsPersistenceUnit().close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return (null);
        }

        return (new Fun.Tuple2<>(genesisBlock, transactionMint));
    }
}