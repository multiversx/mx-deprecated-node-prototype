package network.elrond.trie;

import junit.framework.TestCase;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.junit.Test;

import java.io.File;
import java.sql.DatabaseMetaData;

public class TriePersistenceTest {
    @Test
    public void testTriePutGet() throws Exception{
        Options options = new Options();
        options.createIfMissing(true);
        Iq80DBFactory factory = new Iq80DBFactory();
        DB dbImpl = factory.open(new File("testTrie1"), options);

        Trie trie1 = new TrieImpl(dbImpl);

        String testString = "12345678901234567890123456789012345678901234567890";

        ((TrieImpl) trie1).update("aaa", testString);
        ((TrieImpl) trie1).update("bb", testString);

        byte[] buff = ((TrieImpl) trie1).get("aaa");

        TestCase.assertEquals("Should not have length != 3", testString.length(), buff.length);

        String data = new String(buff);

        TestCase.assertEquals("Test retrieve data as String should be " + testString, testString, data);

        dbImpl.close();
    }

    @Test
    public void testTriePersistence() throws Exception{
        Options options = new Options();
        options.createIfMissing(true);
        Iq80DBFactory factory = new Iq80DBFactory();
        factory.destroy(new File("testTrie1"), options);
        DB dbImpl = factory.open(new File("testTrie1"), options);

        Trie trie1 = new TrieImpl(dbImpl);

        ((TrieImpl) trie1).update("aaa", "bbb");
        trie1.sync();

        dbImpl.close();

        dbImpl = factory.open(new File("testTrie1"), options);
        trie1 = new TrieImpl(dbImpl);

        byte[] buff = ((TrieImpl) trie1).get("aaa");

        TestCase.assertEquals("Should not have length != 0", 0, buff.length);

        dbImpl.close();
    }
}
