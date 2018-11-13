package network.elrond.p2p;

import network.elrond.p2p.model.P2PRequestMessage;

public interface P2PRequestObjectHandler<T> {
    T get(P2PRequestMessage request);
}
