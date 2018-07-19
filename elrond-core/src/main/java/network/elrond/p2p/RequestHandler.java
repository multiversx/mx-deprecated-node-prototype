package network.elrond.p2p;

import network.elrond.application.AppState;

public interface RequestHandler<R, D> {
    R onRequest(AppState state, D data);
}
