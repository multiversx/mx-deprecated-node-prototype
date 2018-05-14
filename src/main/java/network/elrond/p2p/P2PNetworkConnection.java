package network.elrond.p2p;


import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;



/**
 * Database peer, responsible for the table MetaData.
 */
public class P2PNetworkConnection {
    private static final Logger logger = LoggerFactory.getLogger(P2PNetworkConnection.class);
    /**
     * The peer used to do all the DHT operations.
     */
    public static Peer peer;
    /**
     * Array of the local peers.
     */
    private static Peer[] localPeers;


    private static boolean setupStorage = false;
    //private static long timeRows;
    //private static long timeIndexes;

    public P2PNetworkConnection(Peer[] peers) {
        peer = peers[0];
        localPeers = peers;
    }


    public Peer getPeer() {
        return peer;
    }

    public PeerDHT getPeerDHT(){
        return new PeerBuilderDHT(peer).start();
    }


    public static Object get(PeerDHT peer, String key) throws ClassNotFoundException, IOException {
        FutureGet futureGet = peer.get(Number160.createHash(key)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            Iterator<Data> iterator = futureGet.dataMap().values().iterator();
            if (!iterator.hasNext()) {
                return null;
            }
            Data data = iterator.next();
            return data.object();
        }
        return null;
    }

    public static void put(PeerDHT peer, String key, Object value) throws IOException {
        peer.put(Number160.createHash(key)).data(new Data(value)).start().awaitUninterruptibly();
    }


//    /**
//     * Blocking operation to get the MetaData from the DHT.
//     */
//    public static void fetchTableColumns() {
//        FutureDHT future = peer.get(Number160.createHash("TableColumnsMetaData")).setAll().start();
//        logger.trace("METADATA-FETCH-COLUMNS", "BEGIN", Statement.experiment, future.hashCode());
//        future.awaitUninterruptibly();
//        if (future.isSuccess()) {
//            tabColumns = future.getDataMap();
//            logger.debug("Success fetching Table COLUMNS");
//        } else {
//            // add exception?
//            logger.debug("Failure fetching Table COLUMNS!");
//        }
//        logger.trace("METADATA-FETCH-COLUMNS", "END", Statement.experiment, future.hashCode());
//    }
//
//    /**
//     * Blocking operation to get the MetaData from the DHT.
//     */
//    public static void fetchTableRows() {
//        FutureDHT future = peer.get(Number160.createHash("TableRowsMetaData")).setAll().start();
//        logger.trace("METADATA-FETCH-ROWS", "BEGIN", Statement.experiment, future.hashCode());
//        future.awaitUninterruptibly();
//        if (future.isSuccess()) {
//            tabRows = future.getDataMap();
//            //timeRows = System.currentTimeMillis() + 60000; // 1 min
//            logger.debug("Success fetching Table ROWS");
////            logger.debug("FUTURE ROUTE fetch: " + future.getFutureRouting().getRoutingPath());
////            if(!tabRows.isEmpty()) {
////            	try {
////            		Iterator<Entry<Number160, Data>> it = tabRows.entrySet().iterator();
////            		while(it.hasNext()) {
////            			logger.debug("Rows Metadata: "+((TableRows) it.next().getValue().getObject()).toString());
////            		}
////        		} catch (ClassNotFoundException | IOException e) {
////        			logger.error("Data error", e);
////        		}
////            }
//        } else {
//            //add exception?
//            logger.debug("Failure fetching Table ROWS!");
//        }
//        logger.trace("METADATA-FETCH-ROWS", "END", Statement.experiment, future.hashCode());
//    }
//
//    /**
//     * Blocking operation to get the MetaData from the DHT.
//     */
//    public static void fetchTableIndexes() {
//        FutureDHT future = peer.get(Number160.createHash("TableIndexesMetaData")).setAll().start();
//        logger.trace("METADATA-FETCH-INDEXES", "BEGIN", Statement.experiment, future.hashCode());
//        future.awaitUninterruptibly();
//        if (future.isSuccess()) {
//            tabIndexes = future.getDataMap();
//            //timeIndexes = System.currentTimeMillis() + 60000; // 1 min
//            if (!setupStorage) {
//                doSetupStorage();
//            }
//            logger.debug("Success fetching Table INDEXES");
//        } else {
//            //add exception?
//            logger.debug("Failure fetching Table INDEXES!");
//        }
//        logger.trace("METADATA-FETCH-INDEXES", "END", Statement.experiment, future.hashCode());
//    }
//
//    /**
//     * Non-blocking operation to put the MetaData in the DHT.
//     */
//    public static void updateTableColumns() {
//        FutureDHT future = peer.put(Number160.createHash("TableColumnsMetaData")).setDataMap(tabColumns).start();
//        logger.trace("METADATA-UPDATE-COLUMNS", "BEGIN", Statement.experiment, future.hashCode());
//        future.addListener(new BaseFutureAdapter<FutureDHT>() {
//            @Override
//            public void operationComplete(FutureDHT future) throws Exception {
//                if (future.isSuccess()) {
//                    logger.debug("Success updateing COLUMNS metadata!");
//                } else {
//                    //add exception?
//                    logger.debug("Failed updateing COLUMNS metadata!");
//                }
//                logger.trace("METADATA-UPDATE-COLUMNS", "END", Statement.experiment, future.hashCode());
//            }
//        });
//
//        //TODO BROADCAST!!! TABLE COLUMNS METADATA are updated only with CREATE TABLE, a client must EXPLICIT call FETCH METADATA to see a new table
//    }
//
//    /**
//     * Non-blocking operation to put the MetaData in the DHT.
//     */
//    public static void updateTableRows() {
////    	if(!tabRows.isEmpty()) {
////        	try {
////        		Iterator<Entry<Number160, Data>> it = tabRows.entrySet().iterator();
////        		while(it.hasNext()) {
////        			logger.debug("Rows Metadata before update: "+((TableRows) it.next().getValue().getObject()).toString());
////        		}
////    		} catch (ClassNotFoundException | IOException e) {
////    			logger.error("Data error", e);
////    		}
////        }
//        FutureDHT future = peer.put(Number160.createHash("TableRowsMetaData")).setDataMap(tabRows).start();
//        logger.trace("METADATA-UPDATE-ROWS", "BEGIN", Statement.experiment, future.hashCode());
//        future.addListener(new BaseFutureAdapter<FutureDHT>() {
//            @Override
//            public void operationComplete(FutureDHT future) throws Exception {
//                if (future.isSuccess()) {
//                    logger.debug("Success updateing ROWS metadata!");
////                    logger.debug("FUTURE ROUTE Update: " + future.getFutureRouting().getRoutingPath());
//                } else {
//                    //add exception?
//                    logger.debug("Failed updateing ROWS metadata!");
//                }
//                logger.trace("METADATA-UPDATE-ROWS", "END", Statement.experiment, future.hashCode());
//            }
//        });
//
//    }
//
//    /**
//     * Non-blocking operation to put the MetaData in the DHT.
//     */
//    public static void updateTableIndexes() {
//
//        FutureDHT future = peer.put(Number160.createHash("TableIndexesMetaData")).setDataMap(tabIndexes).start();
//        logger.trace("METADATA-UPDATE-INDEXES", "BEGIN", Statement.experiment, future.hashCode());
//        future.addListener(new BaseFutureAdapter<FutureDHT>() {
//            @Override
//            public void operationComplete(FutureDHT future) throws Exception {
//                if (future.isSuccess()) {
//                    logger.debug("Success updateing INDEXES metadata!");
//                } else {
//                    //add exception?
//                    logger.debug("Failed updateing INDEXES metadata!");
//                }
//                logger.trace("METADATA-UPDATE-INDEXES", "END", Statement.experiment, future.hashCode());
//            }
//        });
//
//    }
//
//    /**
//     * Set Storage capacity of a peer.
//     * Takes the DSTRange of the first table, because it is not possible to define different storage capacity for different tables.
//     */
//    private static void doSetupStorage() {
//        try {
//            TableIndexes index = (TableIndexes) tabIndexes.entrySet().iterator().next().getValue().getObject();
//            setupStorage(index.getDSTRange());
//        } catch (ClassNotFoundException | IOException e) {
//            logger.error("Data error", e);
//        }
//        setupStorage = true;
//    }
//
//    /**
//     * Adds a custom storage class that has a limited storage size according to the blockSize.
//     *
//     * @param blockSize The max. number of elements per node.
//     */
//    private static void setupStorage(final int blockSize) {
//        StorageMemory sm = new StorageMemory() {
//            @Override
//            public StorageGeneric.PutStatus put(final Number160 locationKey, final Number160 domainKey,
//                                                final Number160 contentKey, final Data newData, final PublicKey publicKey,
//                                                final boolean putIfAbsent, final boolean domainProtection) {
//                Map<Number480, Data> map = subMap(locationKey, domainKey, Number160.ZERO, Number160.MAX_VALUE);
//                if (map.size() < blockSize) {
//                    return super.put(locationKey, domainKey, contentKey, newData, publicKey, putIfAbsent,
//                            domainProtection);
//                } else {
//                    return StorageGeneric.PutStatus.FAILED;
//                }
//            }
//
//            @Override
//            public SortedMap<Number480, Data> get(final Number160 locationKey, final Number160 domainKey,
//                                                  final Number160 fromContentKey, final Number160 toContentKey) {
//                return super.get(locationKey, domainKey, fromContentKey, toContentKey);
//            }
//        };
//
//        for (Peer peers : localPeers) {
//            peers.getPeerBean().setStorage(sm);
//        }
//
//    }
}