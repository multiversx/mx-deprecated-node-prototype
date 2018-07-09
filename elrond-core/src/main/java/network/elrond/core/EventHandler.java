package network.elrond.core;

import network.elrond.application.AppState;

public interface EventHandler<D> {
    void onEvent(AppState state, D data);
}
