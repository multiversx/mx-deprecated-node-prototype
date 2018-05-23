package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * The BlockServiceImpl class implements BlockService and is used to maintain Block objects
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-16
 */
public class BlockServiceImpl implements BlockService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Computes the hash of the block with an empty sig field
     * Used in signing/verifying process
     *
     * @return hash as byte array
     */
    public byte[] getHash(Block blk, boolean withHash) {
        String json = AppServiceProvider.getSerializationService().encodeJSON(blk);
        return (Util.SHA3.digest(json.getBytes()));
    }


    public void executeBlock(Blockchain blkc, Block blk) throws Exception {
        BootstrapService bootServ = AppServiceProvider.getBootstrapService();

        if ((blk.getClass() == GenesisBlock.class) && (bootServ.getMaxBlockSizeLocal(blkc).compareTo(BigInteger.ONE) == 0)) {
            //DO MINT

            return;
        }

        //fetch transactions
        //TO DO

        //process block
        //TO DO
    }

}
