package network.elrond.p2p;

public interface P2PRequestObjectHandler<T> {
    T get(P2PRequestMessage request);
}
