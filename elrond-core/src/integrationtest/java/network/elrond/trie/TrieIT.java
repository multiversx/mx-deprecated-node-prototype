package network.elrond.trie;

import network.elrond.db.MockDB;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class TrieIT {

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

    @Test  //  tests saving keys to the file  //
    public void testMasiveUpdateFromDB(){
        boolean massiveUpdateFromDBEnabled = true;

        if(massiveUpdateFromDBEnabled) {
            List<String> randomWords = Arrays.asList(randomDictionary.split(","));
            Map<String, String> testerMap = new HashMap<>();

            TrieImpl trie = new TrieImpl(mockDb);
            Random generator = new Random();

            // Random insertion
            for (int i = 0; i < 50000; ++i ){

                int randomIndex1 = generator.nextInt(randomWords.size());
                int randomIndex2 = generator.nextInt(randomWords.size());

                String word1 = randomWords.get(randomIndex1).trim();
                String word2 = randomWords.get(randomIndex2).trim();

                trie.update(word1, word2);
                testerMap.put(word1, word2);
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

            TrieImpl trie2 = new TrieImpl(mockDb, trie.getRootHash());

            // Assert the result now
            keys = testerMap.keySet().iterator();
            while (keys.hasNext()){

                String mapWord1 = keys.next();
                String mapWord2 = testerMap.get(mapWord1);
                String treeWord2 = new String(trie2.get(mapWord1));

                Assert.assertEquals(mapWord2, treeWord2);
            }
        }
    }
}
