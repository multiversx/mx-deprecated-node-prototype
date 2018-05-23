package network.elrond.blockchain;

import java.io.IOException;

public interface BlockchainService {

    <H extends Object, B> boolean contains(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException;

    <H extends Object, B> void put(H hash, B object, Blockchain blockchain, BlockchainUnitType type) throws IOException;

    <H extends Object, B> B get(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException;
}
