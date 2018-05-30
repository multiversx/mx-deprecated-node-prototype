package network.elrond.account;

import org.iq80.leveldb.*;

import java.io.IOException;

public class MockDB implements org.iq80.leveldb.DB {


    @Override
    public byte[] get(byte[] key) throws DBException {
        return new byte[0];
    }

    @Override
    public byte[] get(byte[] key, ReadOptions options) throws DBException {
        return new byte[0];
    }

    @Override
    public DBIterator iterator() {
        return null;
    }

    @Override
    public DBIterator iterator(ReadOptions options) {
        return null;
    }

    @Override
    public void put(byte[] key, byte[] value) throws DBException {

    }

    @Override
    public void delete(byte[] key) throws DBException {

    }

    @Override
    public void write(WriteBatch updates) throws DBException {

    }

    @Override
    public WriteBatch createWriteBatch() {
        return null;
    }

    @Override
    public Snapshot put(byte[] key, byte[] value, WriteOptions options) throws DBException {
        return null;
    }

    @Override
    public Snapshot delete(byte[] key, WriteOptions options) throws DBException {
        return null;
    }

    @Override
    public Snapshot write(WriteBatch updates, WriteOptions options) throws DBException {
        return null;
    }

    @Override
    public Snapshot getSnapshot() {
        return null;
    }

    @Override
    public long[] getApproximateSizes(Range... ranges) {
        return new long[0];
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    @Override
    public void suspendCompactions() throws InterruptedException {

    }

    @Override
    public void resumeCompactions() {

    }

    @Override
    public void compactRange(byte[] begin, byte[] end) throws DBException {

    }

    @Override
    public void close() throws IOException {

    }
}
