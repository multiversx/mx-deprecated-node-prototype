package network.elrond.trie;

import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.AccountStateService;
import network.elrond.db.MockDB;
import network.elrond.service.AppServiceProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;


public class TrieTest {

    private static String LONG_STRING = "1234567890abcdefghijklmnopqrstuvwxxzABCEFGHIJKLMNOPQRSTUVWXYZ";
    private static String ROOT_HASH_EMPTY = "";
    private final String randomDictionary = "spinneries, archipenko, prepotency, herniotomy, preexpress, relaxative, insolvably, debonnaire, apophysate, virtuality, cavalryman, utilizable, diagenesis, vitascopic, governessy, abranchial, cyanogenic, gratulated, signalment, predicable, subquality, crystalize, prosaicism, oenologist, repressive, impanelled, cockneyism, bordelaise, compigne, konstantin, predicated, unsublimed, hydrophane, phycomyces, capitalise, slippingly, untithable, unburnable, deoxidizer, misteacher, precorrect, disclaimer, solidified, neuraxitis, caravaning, betelgeuse, underprice, uninclosed, acrogynous, reirrigate, dazzlingly, chaffiness, corybantes, intumesced, intentness, superexert, abstrusely, astounding, pilgrimage, posttarsal, prayerless, nomologist, semibelted, frithstool, unstinging, ecalcarate, amputating, megascopic, graphalloy, platteland, adjacently, mingrelian, valentinus, appendical, unaccurate, coriaceous, waterworks, sympathize, doorkeeper, overguilty, flaggingly, admonitory, aeriferous, normocytic, parnellism, catafalque, odontiasis, apprentice, adulterous, mechanisma, wilderness, undivorced, reinterred, effleurage, pretrochal, phytogenic, swirlingly, herbarized, unresolved, classifier, diosmosing, microphage, consecrate, astarboard, predefying, predriving, lettergram, ungranular, overdozing, conferring, unfavorite, peacockish, coinciding, erythraeum, freeholder, zygophoric, imbitterer, centroidal, appendixes, grayfishes, enological, indiscreet, broadcloth, divulgated, anglophobe, stoopingly, bibliophil, laryngitis, separatist, estivating, bellarmine, greasiness, typhlology, xanthation, mortifying, endeavorer, aviatrices, unequalise, metastatic, leftwinger, apologizer, quatrefoil, nonfouling, bitartrate, outchiding, undeported, poussetted, haemolysis, asantehene, montgomery, unjoinable, cedarhurst, unfastener, nonvacuums, beauregard, animalized, polyphides, cannizzaro, gelatinoid, apologised, unscripted, tracheidal, subdiscoid, gravelling, variegated, interabang, inoperable, immortelle, laestrygon, duplicatus, proscience, deoxidised, manfulness, channelize, nondefense, ectomorphy, unimpelled, headwaiter, hexaemeric, derivation, prelexical, limitarian, nonionized, prorefugee, invariably, patronizer, paraplegia, redivision, occupative, unfaceable, hypomnesia, psalterium, doctorfish, gentlefolk, overrefine, heptastich, desirously, clarabelle, uneuphonic, autotelism, firewarden, timberjack, fumigation, drainpipes, spathulate, novelvelle, bicorporal, grisliness, unhesitant, supergiant, unpatented, womanpower, toastiness, multichord, paramnesia, undertrick, contrarily, neurogenic, gunmanship, settlement, brookville, gradualism, unossified, villanovan, ecospecies, organising, buckhannon, prefulfill, johnsonese, unforegone, unwrathful, dunderhead, erceldoune, unwadeable, refunction, understuff, swaggering, freckliest, telemachus, groundsill, outslidden, bolsheviks, recognizer, hemangioma, tarantella, muhammedan, talebearer, relocation, preemption, chachalaca, septuagint, ubiquitous, plexiglass, humoresque, biliverdin, tetraploid, capitoline, summerwood, undilating, undetested, meningitic, petrolatum, phytotoxic, adiphenine, flashlight, protectory, inwreathed, rawishness, tendrillar, hastefully, bananaquit, anarthrous, unbedimmed, herborized, decenniums, deprecated, karyotypic, squalidity, pomiferous, petroglyph, actinomere, peninsular, trigonally, androgenic, resistance, unassuming, frithstool, documental, eunuchised, interphone, thymbraeus, confirmand, expurgated, vegetation, myographic, plasmagene, spindrying, unlackeyed, foreknower, mythically, albescence, rebudgeted, implicitly, unmonastic, torricelli, mortarless, labialized, phenacaine, radiometry, sluggishly, understood, wiretapper, jacobitely, unbetrayed, stadholder, directress, emissaries, corelation, sensualize, uncurbable, permillage, tentacular, thriftless, demoralize, preimagine, iconoclast, acrobatism, firewarden, transpired, bluethroat, wanderjahr, groundable, pedestrian, unulcerous, preearthly, freelanced, sculleries, avengingly, visigothic, preharmony, bressummer, acceptable, unfoolable, predivider, overseeing, arcosolium, piriformis, needlecord, homebodies, sulphation, phantasmic, unsensible, unpackaged, isopiestic, cytophagic, butterlike, frizzliest, winklehawk, necrophile, mesothorax, cuchulainn, unrentable, untangible, unshifting, unfeasible, poetastric, extermined, gaillardia, nonpendent, harborside, pigsticker, infanthood, underrower, easterling, jockeyship, housebreak, horologium, undepicted, dysacousma, incurrable, editorship, unrelented, peritricha, interchaff, frothiness, underplant, proafrican, squareness, enigmatise, reconciled, nonnumeral, nonevident, hamantasch, victualing, watercolor, schrdinger, understand, butlerlike, hemiglobin, yankeeland";
    private static String c = "c";
    private static String ca = "ca";
    private static String cat = "cat";
    private static String dog = "dog";
    private static String doge = "doge";
    private static String test = "test";
    private static String dude = "dude";

    private MockDB mockDb = new MockDB();
    private MockDB mockDb_2 = new MockDB();

//		ROOT: [ '\x16', A ]
//		A: [ '', '', '', '', B, '', '', '', C, '', '', '', '', '', '', '', '' ]
//		B: [ '\x00\x6f', D ]
//		D: [ '', '', '', '', '', '', E, '', '', '', '', '', '', '', '', '', 'verb' ]
//		E: [ '\x17', F ]
//		F: [ '', '', '', '', '', '', G, '', '', '', '', '', '', '', '', '', 'puppy' ]
//		G: [ '\x35', 'coin' ]
//		C: [ '\x20\x6f\x72\x73\x65', 'stallion' ]

    @After
    public void closeMockDb() throws IOException {
        mockDb.close();
        mockDb_2.close();
    }

    @Test
    public void testAccountState() {

        AccountStateService accountStateService = AppServiceProvider.getAccountStateService();

        String address = "0x00";
        BigInteger value = BigInteger.valueOf(5);

        AccountState accountState = new AccountState(AccountAddress.EMPTY_ADDRESS);
        accountState.addToBalance(value);

        TrieImpl merkleTrieOfState = new TrieImpl(null);
        merkleTrieOfState.update(address.getBytes(), accountStateService.convertAccountStateToRLP(accountState));

        System.out.println("roothash:  => " + new String(Base64.encode(merkleTrieOfState.getRootHash())));

        AccountState accountStateDecoded = accountStateService.convertToAccountStateFromRLP(merkleTrieOfState.get(address));

        assertEquals(accountState.getNonce(), accountStateDecoded.getNonce());
        assertEquals(accountState.getBalance(), accountStateDecoded.getBalance());

        System.out.println(accountStateDecoded.toString());
    }

    @Test
    public void testExample() {
        TrieImpl trie = new TrieImpl(mockDb);

        byte[] b1 = {10, 7, 1, 1, 3, 5, 5};
        trie.update(b1, "45.0 ETH".getBytes());

        byte[] b2 = {10, 7, 7, 13, 3, 3, 7};
        trie.update(b2, "1.00 WEI".getBytes());

        byte[] b3 = {10, 7, 15, 9, 3, 6, 5};
        trie.update(b3, "1.1 ETH".getBytes());

        byte[] b4 = {10, 7, 7, 13, 3, 9, 7};
        trie.update(b4, "0.12 ETH".getBytes());

        assertEquals("45.0 ETH", new String(trie.get(b1)));
        assertEquals("1.00 WEI", new String(trie.get(b2)));
        assertEquals("1.1 ETH", new String(trie.get(b3)));
        assertEquals("0.12 ETH", new String(trie.get(b4)));
        
        String dmp = trie.getTrieDump();
        System.out.println(dmp);
    }


    @Test
    public void testEmptyKey() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update("", dog);
        assertEquals(dog, new String(trie.get("")));
    }

    @Test
    public void testInsertShortString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));
    }

    @Test
    public void testInsertLongString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
    }

    @Test
    public void testInsertMultipleItems1() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));

        trie.update(doge, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(doge)));

        trie.update(test, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(test)));

        // Test if everything is still there
        assertEquals(dude, new String(trie.get(ca)));
        assertEquals(dog, new String(trie.get(cat)));
        assertEquals(test, new String(trie.get(dog)));
        assertEquals(LONG_STRING, new String(trie.get(doge)));
        assertEquals(LONG_STRING, new String(trie.get(test)));
    }

    @Test
    public void testInsertMultipleItems2() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(doge, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(doge)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));

        trie.update(test, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(test)));

        // Test if everything is still there
        assertEquals(dog, new String(trie.get(cat)));
        assertEquals(dude, new String(trie.get(ca)));
        assertEquals(LONG_STRING, new String(trie.get(doge)));
        assertEquals(test, new String(trie.get(dog)));
        assertEquals(LONG_STRING, new String(trie.get(test)));
    }

    @Test
    public void testUpdateShortToShortString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(cat, dog+"1");
        assertEquals(dog+"1", new String(trie.get(cat)));
    }

    @Test
    public void testUpdateLongToLongString() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
        trie.update(cat, LONG_STRING+"1");
        assertEquals(LONG_STRING+"1", new String(trie.get(cat)));
    }

    @Test
    public void testUpdateShortToLongString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(cat, LONG_STRING+"1");
        assertEquals(LONG_STRING+"1", new String(trie.get(cat)));
    }

    @Test
    public void testUpdateLongToShortString() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));

        trie.update(cat, dog+"1");
        assertEquals(dog+"1", new String(trie.get(cat)));
    }

    @Test
    public void testDeleteShortString1() {
        String ROOT_HASH_BEFORE = "9ed867066c8d6a83e691169ee32049808292d042c2e144eb9df5a07057c7564a";
        String ROOT_HASH_AFTER = "92f3b30494c020742a05b2daa479e267869919708be635370f2719bacf9daa47";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        assertEquals("", new String(trie.get(ca)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteShortString2() {
        String ROOT_HASH_BEFORE = "9ed867066c8d6a83e691169ee32049808292d042c2e144eb9df5a07057c7564a";
        String ROOT_HASH_AFTER = "0bfb8430292e6dfe839906799ed32b8c3294caab31f86008efc389784ad4e36c";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(cat);
        assertEquals("", new String(trie.get(cat)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteShortString3() {
        String ROOT_HASH_BEFORE = "8df95d03fdbc3162da225b67a9a1ca2dd97fa4cdae32b880d9b7c9d5e7bae8e2";
        String ROOT_HASH_AFTER = "e92cc2af3e4fe55e50db207e17a53eead781e6c79fdd0f2d50ecbe3fc019f29c";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dude);
        assertEquals(dude, new String(trie.get(cat)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(dog);
        assertEquals("", new String(trie.get(dog)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteLongString1() {
        String ROOT_HASH_BEFORE = "1327b24fed10cd5dee93d7e28d90500a237b40342f56f4a3ea59b9e16a24fd72";
        String ROOT_HASH_AFTER = "bd220d13ec06f23d8f8d9711ed060bed8685d5895d6ae5d92a95a25f933c403f";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));

        trie.update(dog, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(dog)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(dog);
        assertEquals("", new String(trie.get(dog)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteLongString2() {
        String ROOT_HASH_BEFORE = "f36c3163f43d05de4e042d28048340f371bee82d4ab673a620190d7baa3b39c7";
        String ROOT_HASH_AFTER = "c90824d75a2b1c8754cbfefad92009e487681ae4f72c24dc3afac8adadb1ae6c";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(ca, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(ca)));

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(cat);
        assertEquals("", new String(trie.get(cat)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteLongString3() {
        String ROOT_HASH_BEFORE = "f36c3163f43d05de4e042d28048340f371bee82d4ab673a620190d7baa3b39c7";
        String ROOT_HASH_AFTER = "bd220d13ec06f23d8f8d9711ed060bed8685d5895d6ae5d92a95a25f933c403f";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));

        trie.update(ca, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(ca)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        assertEquals("", new String(trie.get(ca)));
        assertEquals(ROOT_HASH_AFTER, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteMultipleItems1() {
        String ROOT_HASH_BEFORE = "e11aedcc797da02d89d6ad3632caa4fce55047a3239f9b2f746c049d3ad81068";
        String ROOT_HASH_AFTER1 = "471853a2bd94ee9075a2bb43a586bf3a169ef4653343e0149701c9edfce0c8f7";
        String ROOT_HASH_AFTER2 = "5dcadb47126889533b08ae96c6c30ee8f1053aa91e56b371ca365bf648023b63";
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, dog);
        assertEquals(dog, new String(trie.get(cat)));

        trie.update(ca, dude);
        assertEquals(dude, new String(trie.get(ca)));

        trie.update(doge, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(doge)));

        trie.update(dog, test);
        assertEquals(test, new String(trie.get(dog)));

        trie.update(test, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(test)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(dog);
        assertEquals("", new String(trie.get(dog)));
        assertEquals(ROOT_HASH_AFTER1, Hex.toHexString(trie.getRootHash()));

        trie.delete(test);
        assertEquals("", new String(trie.get(test)));
        assertEquals(ROOT_HASH_AFTER2, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteMultipleItems2() {
        String ROOT_HASH_BEFORE = "25d3c752fe6b31631b7a90e8ab526a2024b54788f364c2cba3309afe794c2cb0";
        String ROOT_HASH_AFTER1 = "25ca752247e600cb550b815bfb7361cd5d9b656d4c92556f389bb3d7950d07c1";
        String ROOT_HASH_AFTER2 = "ae9fbd55a563afb54b3833ca730ae625b53ca4a186f28c2dece9400196321155";

        TrieImpl trie = new TrieImpl(mockDb);
        trie.update(c, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(c)));

        trie.update(ca, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(ca)));

        trie.update(cat, LONG_STRING);
        assertEquals(LONG_STRING, new String(trie.get(cat)));
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        assertEquals("", new String(trie.get(ca)));
        assertEquals(ROOT_HASH_AFTER1, Hex.toHexString(trie.getRootHash()));

        trie.delete(cat);
        assertEquals("", new String(trie.get(cat)));
        assertEquals(ROOT_HASH_AFTER2, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testDeleteAll() {
        String ROOT_HASH_BEFORE = "5dcadb47126889533b08ae96c6c30ee8f1053aa91e56b371ca365bf648023b63";
        TrieImpl trie = new TrieImpl(mockDb);
        assertEquals(ROOT_HASH_EMPTY, Hex.toHexString(trie.getRootHash()));

        trie.update(ca, dude);
        trie.update(cat, dog);
        trie.update(doge, LONG_STRING);
        Hex.toHexString(trie.getRootHash());
        assertEquals(ROOT_HASH_BEFORE, Hex.toHexString(trie.getRootHash()));

        trie.delete(ca);
        trie.delete(cat);
        trie.delete(doge);
        assertEquals(ROOT_HASH_EMPTY, Hex.toHexString(trie.getRootHash()));
    }

    @Test
    public void testTrieEquals() {
        TrieImpl trie1 = new TrieImpl(mockDb);
        TrieImpl trie2 = new TrieImpl(mockDb);

        trie1.update(doge, LONG_STRING);
        trie2.update(doge, LONG_STRING);
        assertTrue("Expected tries to be equal", trie1.equals(trie2));
        assertEquals(Hex.toHexString(trie1.getRootHash()), Hex.toHexString(trie2.getRootHash()));

        trie1.update(dog, LONG_STRING);
        trie2.update(cat, LONG_STRING);
        assertFalse("Expected tries not to be equal", trie1.equals(trie2));
        assertNotEquals(Hex.toHexString(trie1.getRootHash()), Hex.toHexString(trie2.getRootHash()));
    }

    @Test
    public void testTrieSync() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(dog, LONG_STRING);
        assertEquals("Expected no data in database", mockDb.getAddedItems(), 0);

        trie.sync();
        assertNotEquals("Expected data to be persisted", mockDb.getAddedItems(), 0);
    }

    @Test
    public void TestTrieDirtyTracking() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(dog, LONG_STRING);
        assertTrue("Expected trie to be dirty", trie.getCache().isDirty());

        trie.sync();
        assertFalse("Expected trie not to be dirty", trie.getCache().isDirty());

        trie.update(test, LONG_STRING);
        assertTrue("Expected trie to be dirty", trie.getCache().isDirty());

//        trie.getCache().undo();
        trie.undo();
        assertFalse("Expected trie not to be dirty", trie.getCache().isDirty());
    }

    @Test
    public void TestTrieReset() {
        TrieImpl trie = new TrieImpl(mockDb);

        trie.update(cat, LONG_STRING);
        assertNotEquals("Expected cached nodes", 0, trie.getCache().getNodes().size());

        trie.getCache().undo();

        assertEquals("Expected no nodes after undo", 0, trie.getCache().getNodes().size());
    }

    @Test
    public void testTrieCopy() {
        TrieImpl trie = new TrieImpl(mockDb);
        trie.update("doe", "reindeer");
        TrieImpl trie2 = trie.copy();
        assertNotEquals(trie.hashCode(), trie2.hashCode()); // avoid possibility that its just a reference copy
        assertEquals(Hex.toHexString(trie.getRootHash()), Hex.toHexString(trie2.getRootHash()));
        assertTrue(trie.equals(trie2));
    }

    @Test
    public void testMasiveUpdate(){
        boolean massiveUpdateTestEnabled = false;

        if(massiveUpdateTestEnabled) {
            List<String> randomWords = Arrays.asList(randomDictionary.split(","));
            HashMap<String, String> testerMap = new HashMap<>();

            TrieImpl trie = new TrieImpl(mockDb);
            Random generator = new Random();

            // Random insertion
            for (int i = 0; i < 100000; ++i ){

                int randomIndex1 = generator.nextInt(randomWords.size());
                int randomIndex2 = generator.nextInt(randomWords.size());

                String word1 = randomWords.get(randomIndex1).trim();
                String word2 = randomWords.get(randomIndex2).trim();

                trie.update(word1, word2);
                testerMap.put(word1, word2);
            }

            int half = testerMap.size() / 2;
            for (int r = 0; r < half; ++r){

                int randomIndex = generator.nextInt(randomWords.size());
                String word1 = randomWords.get(randomIndex).trim();

                testerMap.remove(word1);
                trie.delete(word1);
            }

            trie.cleanCache();
            trie.sync();

            // Assert the result now
            Iterator<String> keys = testerMap.keySet().iterator();
            while (keys.hasNext()){

                String mapWord1 = keys.next();
                String mapWord2 = testerMap.get(mapWord1);
                String treeWord2 = new String(trie.get(mapWord1));

                Assert.assertEquals(mapWord2, treeWord2);
            }
        }
    }
}
