package network.elrond;

import junit.framework.TestCase;
import network.elrond.data.model.Transaction;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LargeHashSetTest {

    private static Logger logger = LogManager.getLogger(LargeHashSetTest.class);

    @Test
    public void testHashSet(){
        Map<String, Object> map = new LRUMap<>(100000);

        long dt1 = System.currentTimeMillis();

        Object dummyObject = new Object();

        int max = 10000;
        int percent = 0;
        int oldPercent = 0;

        List<String> hashesToBeRemoved = new ArrayList<>();

        for (int i = 0; i < max; i++){
            Transaction transaction = new Transaction("a","b", BigInteger.valueOf(i),BigInteger.ZERO, new Shard(0), new Shard(0));
            //hashSet.add(AppServiceProvider.getSerializationService().getHashString(transaction));
            String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
            map.put(hash, dummyObject);

            percent = i * 100 / max;

            if (percent != oldPercent) {
                logger.info("Generating...{}%", percent);
                oldPercent = percent;
            }

            if (i % 10 == 0){
                hashesToBeRemoved.add(hash);
            }
        }

        long dt2 = System.currentTimeMillis();

        logger.debug("Generation took {} ms", dt2-dt1);

        Transaction transaction = new Transaction("a","b", BigInteger.valueOf(max/2),BigInteger.ZERO, new Shard(0), new Shard(0));
        String hashSearch = AppServiceProvider.getSerializationService().getHashString(transaction);

        long dt3 = System.currentTimeMillis();

        TestCase.assertTrue(map.containsKey(hashSearch));


        long dt4 = System.currentTimeMillis();

        logger.debug("Search took {} ms", dt4-dt3);
        logger.debug("Memory {} kB", max * hashSearch.length() / 1024);



    }
}
