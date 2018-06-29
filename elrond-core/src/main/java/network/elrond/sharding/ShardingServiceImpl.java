package network.elrond.sharding;

import network.elrond.core.ObjectUtil;
import network.elrond.data.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class ShardingServiceImpl implements ShardingService {

    Logger logger = LoggerFactory.getLogger(ShardingServiceImpl.class);

    public static Integer MAX_ACTIVE_SHARDS_CONT = 1;

    @Override
    public Shard getShard(byte[] address) {
        BigInteger index = new BigInteger(address);
        int _index = index.mod(BigInteger.valueOf(MAX_ACTIVE_SHARDS_CONT)).intValue();
        return new Shard(_index);
    }

    @Override
    public ShardOperation getShardOperation(Shard shard, Transaction transaction) {

        Shard receiverShard = transaction.getReceiverShard();
        Shard senderShard = transaction.getSenderShard();

        if (ObjectUtil.isEqual(senderShard, receiverShard)) {
            return ShardOperation.INTRA_SHARD;
        }

        if (ObjectUtil.isEqual(shard, receiverShard)) {
            return ShardOperation.INTER_SHARD_IN;
        }

        if (ObjectUtil.isEqual(shard, senderShard)) {
            return ShardOperation.INTER_SHARD_OUT;
        }


        throw new RuntimeException("Not supported operation !");
    }


}
