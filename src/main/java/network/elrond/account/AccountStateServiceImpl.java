package network.elrond.account;

import network.elrond.core.LRUMap;
import network.elrond.core.RLP;
import network.elrond.core.RLPList;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigInteger;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class AccountStateServiceImpl implements AccountStateService {


    public byte[] getHash(AccountState state) {
        String json = AppServiceProvider.getSerializationService().encodeJSON(state);
        return (Util.SHA3.digest(json.getBytes()));
    }


    @Override
    public synchronized AccountState getOrCreateAccountState(String address, Accounts accounts) throws IOException, ClassNotFoundException {
        AccountState state = getAccountState(address, accounts);
        if (state == null) {
            setAccountState(address, new AccountState(), accounts);
        }

        return getAccountState(address, accounts);
    }

    @Override
    public synchronized AccountState getAccountState(String address, Accounts accounts) {
        AccountsPersistenceUnit<String, AccountState> unit = accounts.getAccountsPersistenceUnit();

        LRUMap<String, AccountState> cache = unit.cache;

        boolean exists = cache.get(address) != null;
        if (!exists) {
            AccountState object = getDataFromDatabase(address, unit);
            if (object != null) {
                cache.put(address, object);
            }
        }
        return cache.get(address);
    }


    @Override
    public synchronized void rollbackAccountStates(Accounts accounts) {

        AccountsPersistenceUnit<String, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.queue.clear();

        for (String address : unit.cache.keySet()) {

            AccountState state = unit.cache.get(address);
            if (!state.isDirty()) {
                continue;
            }

            AccountState object = getDataFromDatabase(address, unit);
            unit.cache.put(address, object);

        }

    }


    @Override
    public synchronized void commitAccountStates(Accounts accounts) {
        AccountsPersistenceUnit<String, AccountState> unit = accounts.getAccountsPersistenceUnit();

        for (Fun.Tuple2<String, AccountState> entry = unit.queue.poll(); entry != null; entry = unit.queue.poll()) {

            String address = entry.a;
            AccountState state = entry.b;
            if (!state.isDirty()) {
                continue;
            }

            String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(state);
            unit.database.put(bytes(address), bytes(strJSONData));
        }

    }


    @Override
    public synchronized void setAccountState(String address, AccountState state, Accounts accounts) {


        if (address == null || state == null) {
            return;
        }

        AccountsPersistenceUnit<String, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.cache.put(address, state);
        state.setDirty(true);
        unit.scheduleForPersistence(address, state);


    }


    private <A extends String> AccountState getDataFromDatabase(A address, AccountsPersistenceUnit<A, AccountState> unit) {
        byte[] data = unit.database.get(bytes(address));
        if (data == null) {
            return null;
        }
        String strJSONData = asString(data);
        return decodeObject(AccountState.class, strJSONData);
    }

    private <B> B decodeObject(Class<B> clazz, String strJSONData) {
        if (strJSONData == null) {
            return null;
        }
        return AppServiceProvider.getSerializationService().decodeJSON(strJSONData, clazz);
    }

    public byte[] getRLPencoded(AccountState accountState) {
        byte[] nonce = RLP.encodeBigInteger(accountState.getNonce());
        byte[] balance = RLP.encodeBigInteger(accountState.getBalance());

        return RLP.encodeList(nonce, balance);
    }

    public AccountState getAccountStateFromRLP(byte[] rlpData) {
        AccountState accountState = new AccountState();

        RLPList items = (RLPList) RLP.decode2(rlpData).get(0);
        accountState.setNonce(new BigInteger(1, ((items.get(0).getRLPData()) == null ? new byte[]{0} :
                items.get(0).getRLPData())));
        accountState.setBalance(new BigInteger(1, ((items.get(1).getRLPData()) == null ? new byte[]{0} :
                items.get(1).getRLPData())));

        return (accountState);
    }


}
