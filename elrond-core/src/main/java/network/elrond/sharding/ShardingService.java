package network.elrond.sharding;

import network.elrond.account.AccountAddress;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.Transaction;

public interface ShardingService {

    Shard getShard(byte[] address);

    ShardOperation getShardOperation(Shard shard, Transaction transaction);

    PublicKey getPublicKeyForMinting(Shard shard);

    PrivateKey getPrivateKeyForMinting(Shard shard);

    AccountAddress getAddressForMinting(Shard shard);
}
