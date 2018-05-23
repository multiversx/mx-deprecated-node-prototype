package network.elrond.account;

import network.elrond.core.LRUMap;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.mapdb.Fun;

import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class AccountStateServiceImpl implements AccountStateService {


    public byte[] getHash(AccountState state) {
        String json = AppServiceProvider.getSerializationService().encodeJSON(state);
        return (Util.SHA3.digest(json.getBytes()));
    }


    @Override
    public synchronized <A extends String> AccountState getOrCreateAccountState(A address, Accounts<A> accounts) throws IOException, ClassNotFoundException {
        AccountState state = getAccountState(address, accounts);
        if (state == null) {
            setAccountState(address, new AccountState(), accounts);
        }

        return getAccountState(address, accounts);
    }

    @Override
    public synchronized <A extends String> AccountState getAccountState(A address, Accounts<A> accounts) throws IOException, ClassNotFoundException {
        AccountsPersistenceUnit<A, AccountState> unit = accounts.getAccountsPersistenceUnit();

        LRUMap<A, AccountState> cache = unit.cache;

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
    public synchronized <A extends String> void rollbackAccountStates(Accounts<A> accounts) {

        AccountsPersistenceUnit<A, AccountState> unit = accounts.getAccountsPersistenceUnit();
        unit.queue.clear();

        for (A address : unit.cache.keySet()) {

            AccountState state = unit.cache.get(address);
            if (!state.isDirty()) {
                continue;
            }

            AccountState object = getDataFromDatabase(address, unit);
            unit.cache.put(address, object);

        }

    }


    @Override
    public synchronized <A extends String> void commitAccountStates(Accounts<A> accounts) {
        AccountsPersistenceUnit<A, AccountState> unit = accounts.getAccountsPersistenceUnit();

        for (Fun.Tuple2<A, AccountState> entry = unit.queue.poll(); entry != null; entry = unit.queue.poll()) {

            A address = entry.a;
            AccountState state = entry.b;
            if (!state.isDirty()) {
                continue;
            }

            String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(state);
            unit.database.put(bytes(address), bytes(strJSONData));
        }

    }


    @Override
    public synchronized <A extends String> void setAccountState(A address, AccountState state, Accounts<A> accounts) {


        if (address == null || state == null) {
            return;
        }

        AccountsPersistenceUnit<A, AccountState> unit = accounts.getAccountsPersistenceUnit();
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


}
