package network.elrond.blockchain;

import javafx.util.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface BlockchainService {

    <H extends Object, B extends Serializable> boolean contains(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException;

    <H extends Object, B extends Serializable> void put(H hash, B object, Blockchain blockchain, BlockchainUnitType type) throws IOException;

    <H extends Object, B extends Serializable> void putLocal(H hash, B object, Blockchain blockchain, BlockchainUnitType type);

    <H extends Object, B extends Serializable> B get(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException;

    <H extends Object, B extends Serializable> B getLocal(H hash, Blockchain blockchain, BlockchainUnitType type);

    <H extends Object, B extends Serializable> List<Pair<H, B>>  getAll(List<H> hashes, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException;
}
