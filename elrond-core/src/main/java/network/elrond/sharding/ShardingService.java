package network.elrond.sharding;

import network.elrond.data.Transaction;

public interface ShardingService {

    Shard getShard(byte[] address);

    ShardOperation getShardOperation(Shard shard, Transaction transaction);

    Integer getNumberOfShards();

}
