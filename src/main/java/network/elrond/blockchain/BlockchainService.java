package network.elrond.blockchain;

import java.io.IOException;

public interface BlockchainService {

    <H extends String, B> boolean contains(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException;

    <H extends String, B> void put(H hash, B object, Blockchain blockchain, BlockchainUnitType type) throws IOException;

    <H extends String, B> B get(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException;
}
