package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.blockchain.SettingsType;
import network.elrond.core.Util;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.AppTask;
import network.elrond.processor.AppTasks;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrappingProcessor implements AppTask {

    private Logger logger = LoggerFactory.getLogger(AppTasks.class);

    @Override
    public void process(Application application) throws IOException {
        AppState state = application.getState();

        BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        BlockchainService appPersistanceService = AppServiceProvider.getAppPersistanceService();
        TransactionService transactionService = AppServiceProvider.getTransactionService();
        //BlockService blockService = AppServiceProvider.getBlockService();
        SerializationService serializationService = AppServiceProvider.getSerializationService();

        P2PBroadcastChanel chanTx = state.getChanel(P2PChannelName.TRANSACTION);
        P2PBroadcastChanel blkTx = state.getChanel(P2PChannelName.BLOCK);

        Thread threadProcess = new Thread(() -> {

            while (state.isStillRunning()) {
                BigInteger maxBlkHeightNetw = Util.BIG_INT_MIN_ONE;
                BigInteger maxBlkHeightLocal = BigInteger.ZERO;

                try {
                    maxBlkHeightNetw = bootstrapService.getMaxBlockSizeNetwork(state.getConnection());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    maxBlkHeightLocal = bootstrapService.getMaxBlockSizeLocal(state.getBlockchain());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if ((maxBlkHeightLocal.compareTo(BigInteger.ZERO) > 0) && (maxBlkHeightNetw.compareTo(BigInteger.ONE) < 0)) {
                    //broken connection, wait 5 secs
                    logger.warn("Broken connection detected! Waiting 5 seconds...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (maxBlkHeightLocal.compareTo(BigInteger.ZERO) == 0) {
                    //put GENESIS block
                    GenesisBlock gb = new GenesisBlock();

                    //put locally not broadcasting it
                    try {
                        String strHashGB = new String(Base64.encode(serializationService.getHash(gb, true)));
                        appPersistanceService.put(strHashGB, gb, state.getBlockchain(), BlockchainUnitType.BLOCK);
                        appPersistanceService.put(gb.getNonce(), strHashGB, state.getBlockchain(), BlockchainUnitType.BLOCK_INDEX);
                        bootstrapService.setMaxBlockSizeLocal(state.getBlockchain(), BigInteger.ONE);

                        if (maxBlkHeightNetw.compareTo(BigInteger.ONE) < 0) {
                            AppServiceProvider.getP2PObjectService().put(state.getConnection(),
                                    SettingsType.MAX_BLOCK_HEIGHT.toString(), serializationService.encodeJSON(BigInteger.ONE));
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                //if current height < network height - 1
                if (maxBlkHeightNetw.compareTo(maxBlkHeightLocal.add(BigInteger.ONE)) > 0) {
                    logger.warn("Bootstrapping... [local height: " + maxBlkHeightLocal.toString(10) + " > network height: " +
                            maxBlkHeightNetw.toString(10));
                    state.setBootstrapping(true);

                    String strJSON;
                    String strHashHeight;
                    String strHashBlock;

                    for (BigInteger counter = maxBlkHeightLocal.add(BigInteger.ONE); counter.compareTo(maxBlkHeightLocal) < 0; counter = counter.add(BigInteger.ONE)) {
                        //get the hash of the block from network
                        strHashHeight = SettingsType.HEIGHT_BLOCK.toString() + "_" + counter.toString(10);
                        try {
                            strJSON = (String) AppServiceProvider.getP2PObjectService().get(state.getConnection(), strHashHeight);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            break;
                        }

                        if (strJSON == null) {
                            logger.warn("Can not bootstrap! Could not find " + strHashHeight + " on DTH!");
                            break;
                        }
                        strHashBlock = serializationService.decodeJSON(strJSON, String.class);

                        //get the block from the block hash
                        try {
                            strJSON = (String) AppServiceProvider.getP2PObjectService().get(state.getConnection(), strHashBlock);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            break;
                        }

                        if (strJSON == null) {
                            logger.warn("Can not bootstrap! Could not find block with hash " + strHashBlock + "!");
                            break;
                        }

                        Block blk = serializationService.decodeJSON(strJSON, Block.class);

                        try {
                            //blockService.executeBlock(state.getBlockchain(), blk);
                        } catch (Exception ex) {
                            logger.warn("Can not process block hash " + strHashBlock + "! " + ex.getMessage());
                            break;
                        }

                        //block successfully processed!
                        try {
                            appPersistanceService.put(strHashBlock, blk, state.getBlockchain(), BlockchainUnitType.BLOCK);
                            appPersistanceService.put(blk.getNonce(), strHashBlock, state.getBlockchain(), BlockchainUnitType.BLOCK_INDEX);
                            bootstrapService.setMaxBlockSizeLocal(state.getBlockchain(), blk.getNonce());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    state.setBootstrapping(false);

                } else {
                    logger.warn("Nothing else to bootstrap! Waiting 5 seconds...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;

                }

            }
        });
        threadProcess.start();


    }
}
