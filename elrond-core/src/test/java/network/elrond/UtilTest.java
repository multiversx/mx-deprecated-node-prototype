package network.elrond;


import junit.framework.TestCase;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.blockchain.Blockchain;
import network.elrond.consensus.Validator;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class UtilTest {

//    static{
//        ThreadContext.put("LOG_FOLDER", "logs-test");
//    }

    private static Logger logger = LogManager.getLogger(UtilTest.class);


//    static {
//        // Get the process id
//        String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
//
//        // MDC
//        ThreadContext.put("pid", pid);
//    }


    public static void displayListValidators(List<Validator> list) {
        for (int i = 0; i < list.size(); i++) {
            Validator v = list.get(i);

            System.out.println(v.getPubKey() + ", S: " + v.getStake().toString(10) + ", R: " + v.getRating());
        }
        System.out.println();
    }

    @Test
    public void testDisplayStuff1() {
        long currentMillis = System.currentTimeMillis();
        System.out.println(String.format("Event from , time: %1$tY.%1$tm.%1$td %1$tT.%2$03d", new Date(currentMillis), currentMillis % 1000));

        //System.out.println(String.format("%1$03d", 1));
    }

    @Test
    public void testUtilHexToByteArray() {
        TestCase.assertEquals("00", Util.byteArrayToHexString(new byte[]{0}));
        TestCase.assertEquals("0b", Util.byteArrayToHexString(new byte[]{11}));
        TestCase.assertEquals("0f", Util.byteArrayToHexString(new byte[]{15}));
        TestCase.assertEquals("10", Util.byteArrayToHexString(new byte[]{16}));
        TestCase.assertEquals("80", Util.byteArrayToHexString(new byte[]{-128}));
        TestCase.assertEquals("ff", Util.byteArrayToHexString(new byte[]{-1}));
        TestCase.assertEquals("13d18bf84f5643", Util.byteArrayToHexString(new byte[]{19, -47, -117,
                -8, 79, 86, 67}));

        TestCase.assertEquals("13d18bf84f5643", Util.byteArrayToHexString(Util.hexStringToByteArray("13d18bf84f5643")));
    }


    public void testUtilGetAddressFromPublicKey() {
        String strPubKeyHexa = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
        String strAddr = "0xa87b8fa28a8476553363a9356aa02635e4a1b033";

        TestCase.assertEquals(strAddr, Util.getAddressFromPublicKey(Util.hexStringToByteArray(strPubKeyHexa)));
    }

    @Test
    public void testUtilGetAddressFromPublicKeyTestVersion() {
        String strPubKeyHexa = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
        String strAddr = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";

        TestCase.assertEquals(strAddr, Util.getAddressFromPublicKey(Util.hexStringToByteArray(strPubKeyHexa)));
    }

    @Test
    public void testPrint01(){
        logger.debug("{}", Arrays.asList("aaa", "bbb", "ccc"));
    }

    @Test
    public void testAddConcurrentMap(){
        Map<Integer, HashSet<String>> map = new ConcurrentHashMap<>();

        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("aaa");

        map.put(1, hashSet);

        HashSet<String> hashSet2 = new HashSet<>();
        hashSet2.add("aaa");
        hashSet2.add("bbb");
        map.put(1, hashSet2);

        HashSet<String> hashSetResult = map.get(1);
    }

    @Test
    public void testLoggerParameters(){
        SimpleDateFormat sdfSource = new SimpleDateFormat(
                "yyyy-MM-dd hh.mm.ss");


        logger.warn(sdfSource.format(new Date()));
        logger.warn(Util.getHostName());

//        //System.setProperty("logFilename", "/test");
//
//        Properties properties = new Properties();
//        properties.setProperty("property.DEFAULT_LOG_FOLDER", "logs-test");
//
//
//
//        ctx.getConfiguration().getRootLogger().getPropertyList();
//
//        System.getProperties();
//
//
////                .getStrSubstitutor().getVariableResolver()..getProperties().put("DEFAULT_LOG_FOLDER", "test-logs");
//        ctx.reconfigure();

//
//        ThreadContext.put("LOG_FOLDER", "logs-test");
//        org.apache.logging.log4j.core.LoggerContext ctx =
//                (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
//        ctx.reconfigure();

        //logger.traceEntry("Params {} {} {}", null, null, null);

        logger.trace("TRACE {} {}", 1, 2);
        logger.debug("DEBUG {} {}", 1, 2);
        logger.info("INFO {} {}", 1, 2);
        logger.warn("WARN {} {}", 1, 2);
        logger.error("ERROR {} {}", 1, 2);
        logger.fatal("FATAL {} {}", 1, 2);
        logger.throwing(new Exception("aaa"));
        logger.catching(new Exception("bbb"));

        logger.trace(String.format("BigInteger.TEN = %d", BigInteger.TEN));
        //logger.trace(String.format("BigInteger.TEN = %d", null));

        logger.trace("Exit, returns: " + logger.traceExit("A"));
        logger.trace("Exit, returns: " + logger.traceExit((Object) null));

        byte[] buff = new byte[]{0, 1, 2, 3, 4};
        logger.trace("byte array: {}", buff);

    }

    @Test
    public void testStringFormat() {
        logger.info(String.format("test %s %d", (Object) null, (BigInteger) null));
    }

    public static void createDummyGenesisBlock(Blockchain blockchain) {
        Block blockGenesis = new Block();
        blockGenesis.setShard(new Shard(0));
        blockGenesis.setTimestamp(System.currentTimeMillis() - 1000);

        blockchain.setGenesisBlock(blockGenesis);
    }

    public static void printAccountsWithBalance(Accounts accounts) {
        System.out.println("Accounts: ");
        System.out.println("================================================================");

        if (accounts == null) {
            System.out.println(" * NULL accounts object!");
            System.out.println("================================================================");
            return;
        }

        if (accounts.getAddresses().size() == 0) {
            System.out.println(" * EMPTY set!");
            System.out.println("================================================================");
            return;
        }

        AccountState accountState;

        for (AccountAddress accountAddress : accounts.getAddresses()) {

            try {
                accountState = AppServiceProvider.getAccountStateService().getAccountState(accountAddress, accounts);
            } catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }

            if (accountState == null) {
                continue;
            }

            System.out.println(Util.byteArrayToHexString(accountAddress.getBytes()) + ": nonce " +
                    accountState.getNonce().toString(10) + "; balance " +
                    accountState.getBalance().toString(10));
        }


//        for ( entry: nodes.keySet()){
//
//        }
        System.out.println("================================================================");

    }
}
